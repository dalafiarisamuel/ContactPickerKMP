@file:OptIn(ExperimentalForeignApi::class)

package com.devtamuno.kmp.contactpicker.contract

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
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
import platform.UIKit.UIWindow
import platform.UIKit.modalInPresentation
import platform.darwin.NSObject
import platform.posix.memcpy

/**
 * iOS-specific implementation of the [ContactPicker] contract.
 *
 * This implementation leverages the native `CNContactPickerViewController` from the ContactsUI
 * framework to provide a familiar and secure contact selection experience for iOS users.
 */
internal actual class ContactPicker {

    private var singleDelegate: CNContactPickerDelegateProtocol? = null
    private var multiDelegate: CNContactPickerDelegateProtocol? = null

    /**
     * Registers the callback for single contact selection.
     */
    @Composable
    actual fun RegisterContactPicker(onContactSelected: (Contact?) -> Unit) {
        val callback by rememberUpdatedState(onContactSelected)
        remember {
            singleDelegate = SingleContactPickerDelegate { callback(it) }
        }
    }

    /**
     * Displays the native iOS contact picker for single selection.
     */
    actual fun launchContactPicker() {
        val picker = CNContactPickerViewController()
        picker.delegate = singleDelegate
        UIViewController.topMostViewController()?.presentViewController(picker, true, null)
    }

    /**
     * Registers the callback for multiple contact selection.
     */
    @Composable
    actual fun RegisterMultiContactPicker(onContactsSelected: (List<Contact>) -> Unit) {
        val callback by rememberUpdatedState(onContactsSelected)
        remember {
            multiDelegate = MultiContactPickerDelegate { callback(it) }
        }
    }

    /**
     * Displays the native iOS contact picker for multiple selection.
     */
    actual fun launchMultiContactPicker() {
        val picker = CNContactPickerViewController()
        picker.delegate = multiDelegate
        picker.modalInPresentation = true
        UIViewController.topMostViewController()?.presentViewController(picker, true, null)
    }
}

/**
 * Delegate for single contact selection.
 *
 * This delegate implements the [CNContactPickerDelegateProtocol] to handle the selection
 * of a single contact from the native iOS contact picker.
 *
 * **Note:** It only overrides the `contactPicker(_:didSelectContact:)` method. This is crucial
 * because providing an implementation for `contactPicker(_:didSelectContacts:)` (plural)
 * would cause the iOS picker to switch to multi-selection mode, even if only one contact is desired.
 *
 * @param onContactSelected Callback invoked when a contact is selected or the picker is cancelled.
 */
private class SingleContactPickerDelegate(
    private val onContactSelected: (Contact?) -> Unit,
) : NSObject(), CNContactPickerDelegateProtocol {

    override fun contactPicker(
        picker: CNContactPickerViewController,
        didSelectContact: CNContact,
    ) {
        onContactSelected(mapCNContactToContact(didSelectContact))
        picker.delegate = null
        picker.dismissViewControllerAnimated(true, null)
    }

    override fun contactPickerDidCancel(picker: CNContactPickerViewController) {
        picker.delegate = null
        picker.dismissViewControllerAnimated(true, null)
    }
}

/**
 * Delegate for multiple contact selection.
 *
 * This delegate implements the [CNContactPickerDelegateProtocol] to handle the selection
 * of multiple contacts from the native iOS contact picker.
 *
 * By implementing the `contactPicker(_:didSelectContacts:)` method, the iOS contact picker
 * automatically enables its multi-selection user interface, allowing users to select
 * multiple contacts before finishing.
 *
 * @param onContactsSelected Callback invoked with the list of selected contacts.
 */
private class MultiContactPickerDelegate(
    private val onContactsSelected: (List<Contact>) -> Unit,
) : NSObject(), CNContactPickerDelegateProtocol {

    override fun contactPicker(
        picker: CNContactPickerViewController,
        didSelectContacts: List<*>,
    ) {
        val contacts = didSelectContacts
            .mapNotNull { it as? CNContact }
            .map { mapCNContactToContact(it) }

        onContactsSelected(contacts)
        picker.delegate = null
        picker.dismissViewControllerAnimated(true, null)
    }

    override fun contactPickerDidCancel(picker: CNContactPickerViewController) {
        picker.delegate = null
        picker.dismissViewControllerAnimated(true, null)
    }
}

private fun mapCNContactToContact(cnContact: CNContact): Contact {
    val id = cnContact.identifier
    val name = "${cnContact.givenName} ${cnContact.familyName}".trim()
    val phoneNumbers = getPhoneNumbers(cnContact.phoneNumbers)
    val email = getEmailAddress(cnContact.emailAddresses)
    val photoData: ByteArray? = cnContact.thumbnailImageData?.toByteArray()

    return Contact(
        id = id,
        name = name,
        phoneNumbers = phoneNumbers,
        email = email,
        contactAvatar = photoData
    )
}

private fun getPhoneNumbers(contactList: List<*>): List<String> {
    return contactList
        .mapNotNull { (it as? CNLabeledValue)?.value as? CNPhoneNumber }
        .map { it.stringValue }
}

private fun getEmailAddress(emailAddresses: List<*>): List<String> {
    return emailAddresses
        .mapNotNull { (it as? CNLabeledValue)?.value as? NSString }
        .map { it.toString() }
}

private fun NSData.toByteArray(): ByteArray {
    val bytes = ByteArray(this.length.toInt())
    memScoped {
        memcpy(bytes.refTo(0), this@toByteArray.bytes, this@toByteArray.length)
    }
    return bytes
}

private fun UIViewController.Companion.topMostViewController(): UIViewController? {
    val sharedApp = UIApplication.sharedApplication
    val window = sharedApp.keyWindow ?: sharedApp.windows
        .mapNotNull { it as? UIWindow }
        .firstOrNull { it.isKeyWindow() }
        ?: sharedApp.windows.firstOrNull() as? UIWindow

    return findTopMostViewController(window?.rootViewController)
}

private fun findTopMostViewController(root: UIViewController?): UIViewController? {
    var current = root
    while (current != null) {
        val presented = current.presentedViewController
        if (presented != null) {
            current = presented
            continue
        }

        if (current is UINavigationController) {
            val visible = current.visibleViewController
            if (visible != null && visible != current) {
                current = visible
                continue
            }
        }

        if (current is UITabBarController) {
            val selected = current.selectedViewController
            if (selected != null && selected != current) {
                current = selected
                continue
            }
        }

        break
    }
    return current
}
