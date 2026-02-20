@file:OptIn(ExperimentalForeignApi::class)

package com.devtamuno.kmp.contactpicker.contract

import androidx.compose.runtime.Composable
import com.devtamuno.kmp.contactpicker.data.Contact
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.refTo
import platform.Contacts.CNContact
import platform.Contacts.CNLabeledValue
import platform.Contacts.CNPhoneNumber
import platform.ContactsUI.CNContactPickerDelegateProtocol
import platform.ContactsUI.CNContactPickerViewController
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.UIKit.UIApplication
import platform.UIKit.UINavigationController
import platform.UIKit.UITabBarController
import platform.UIKit.UIViewController
import platform.darwin.NSObject
import platform.posix.memcpy

/**
 * iOS-specific implementation of the [ContactPicker] contract.
 *
 * This implementation leverages the native `CNContactPickerViewController` from the ContactsUI 
 * framework to provide a familiar and secure contact selection experience for iOS users.
 * 
 * It acts as both the launcher and the delegate (`CNContactPickerDelegateProtocol`) to handle 
 * lifecycle events and data extraction from the native [CNContact] objects.
 */
internal actual class ContactPicker : NSObject(), CNContactPickerDelegateProtocol {

    private val contactPicker = CNContactPickerViewController()
    private lateinit var onContactSelected: (Contact?) -> Unit

    /**
     * Registers the callback to be invoked when a contact is selected.
     * 
     * In the iOS implementation, this stores the provided [onContactSelected] lambda
     * which will be triggered by the delegate methods once the user interacts with the picker.
     *
     * @param onContactSelected Callback invoked with the selected [Contact] or `null` if cancelled.
     */
    @Composable
    actual fun RegisterContactPicker(onContactSelected: (Contact?) -> Unit) {
        this.onContactSelected = onContactSelected
    }

    /**
     * Displays the native iOS contact picker.
     * 
     * This method resolves the current top-most [UIViewController] and presents the 
     * `CNContactPickerViewController` modally. It also sets this instance as the delegate.
     */
    actual fun launchContactPicker() {
        contactPicker.setDelegate(this)
        UIViewController.topMostViewController()?.presentViewController(contactPicker, true, null)
    }

    /**
     * Invoked by the system when the user cancels the contact picker.
     * 
     * Cleans up the delegate reference and notifies the observer with `null`.
     */
    override fun contactPickerDidCancel(picker: CNContactPickerViewController) {
        onContactSelected(null)
        contactPicker.delegate = null
        picker.dismissViewControllerAnimated(true, null)
    }

    /**
     * Invoked by the system when the user selects a specific contact.
     * 
     * Maps the native [CNContact] properties (identifier, names, phones, emails, and thumbnail) 
     * to the cross-platform [Contact] data model.
     * 
     * @param picker The native picker instance.
     * @param didSelectContact The native contact object returned by iOS.
     */
    override fun contactPicker(
        picker: CNContactPickerViewController,
        didSelectContact: CNContact,
    ) {
        val id = didSelectContact.identifier
        val name = "${didSelectContact.givenName} ${didSelectContact.familyName}".trim()
        val phoneNumbers = getPhoneNumbers(didSelectContact.phoneNumbers)
        val email = getEmailAddress(didSelectContact.emailAddresses)
        val photoData: ByteArray? = didSelectContact.thumbnailImageData?.toByteArray()

        onContactSelected(
            Contact(
                id = id,
                name = name,
                phoneNumbers = phoneNumbers,
                email = email,
                contactAvatar = photoData
            )
        )
        picker.dismissViewControllerAnimated(true, null)
        contactPicker.delegate = null
    }

    /**
     * Utility to extract string representations of phone numbers from [CNLabeledValue]s.
     * 
     * @param contactList A list of [CNLabeledValue] objects where values are [CNPhoneNumber].
     * @return A list of formatted phone number strings.
     */
    private fun getPhoneNumbers(contactList: List<*>): List<String> {
        return contactList
            .mapNotNull { (it as? CNLabeledValue)?.value as? CNPhoneNumber }
            .map { it.stringValue }
    }

    /**
     * Utility to extract string representations of email addresses from [CNLabeledValue]s.
     * 
     * @param emailAddresses A list of [CNLabeledValue] objects where values are [NSString].
     * @return A list of email address strings.
     */
    private fun getEmailAddress(emailAddresses: List<*>): List<String> {
        return emailAddresses
            .mapNotNull { (it as? CNLabeledValue)?.value as? NSString }
            .map { it.toString() }
    }

    /**
     * Converts native [NSData] to a Kotlin [ByteArray].
     * 
     * Uses [memScoped] and [memcpy] for high-performance memory copying from the 
     * Objective-C pointer to the Kotlin managed array.
     */
    private fun NSData.toByteArray(): ByteArray {
        val bytes = ByteArray(this.length.toInt())
        memScoped {
            memcpy(bytes.refTo(0), this@toByteArray.bytes, this@toByteArray.length)
        }
        return bytes
    }

    /**
     * Companion extension to find the currently active [UIViewController] in the view hierarchy.
     */
    private fun UIViewController.Companion.topMostViewController(): UIViewController? {
        return findTopMostViewController(UIApplication.sharedApplication.keyWindow?.rootViewController)
    }

    /**
     * Recursively traverses the view controller hierarchy to find the visible controller.
     * 
     * It handles:
     * - Presented View Controllers (Modals)
     * - [UINavigationController] (Visible Child)
     * - [UITabBarController] (Selected Child)
     * 
     * @param rootViewController The starting point for the search, typically the key window's root.
     * @return The top-most visible [UIViewController].
     */
    private fun findTopMostViewController(rootViewController: UIViewController?): UIViewController? {
        val presented = rootViewController?.presentedViewController
        if (presented != null) {
            return findTopMostViewController(presented)
        }

        return when (rootViewController) {
            is UINavigationController -> findTopMostViewController(rootViewController.visibleViewController)
            is UITabBarController -> findTopMostViewController(rootViewController.selectedViewController)
            else -> rootViewController
        }
    }
}
