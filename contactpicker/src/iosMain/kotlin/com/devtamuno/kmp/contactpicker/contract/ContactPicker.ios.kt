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

internal actual class ContactPicker:  NSObject(), CNContactPickerDelegateProtocol {

    private val contactPicker = CNContactPickerViewController()

    private lateinit var onContactSelected: (Contact?) -> Unit

    @Composable
    actual fun registerContactPicker(onContactSelected: (Contact?) -> Unit) {
        this.onContactSelected = onContactSelected
    }

    actual fun launchContactPicker() {
        contactPicker.setDelegate(this)

        UIViewController.topMostViewController()?.presentViewController(contactPicker, true, null)
    }
    override fun contactPickerDidCancel(picker: CNContactPickerViewController) {
        onContactSelected(null)
        contactPicker.delegate = null
        picker.dismissViewControllerAnimated(true, null)
    }

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

    private fun getPhoneNumbers(contactList: List<*>): List<String> {
        val contactListMapped: List<String?> = contactList.map {
            ((it as? CNLabeledValue)?.value) as? CNPhoneNumber
        }.map {
            it?.stringValue
        }
        return contactListMapped.filterNotNull()
    }

    private fun getEmailAddress(emailAddresses: List<*>): List<String> {
        val emailAddressesListMapped: List<String?> = emailAddresses.map {
            ((it as? CNLabeledValue)?.value) as? NSString
        }.map {
            it?.toString()
        }
        return emailAddressesListMapped.filterNotNull()
    }


    private fun NSData.toByteArray(): ByteArray {
        val bytes = ByteArray(this.length.toInt())
        memScoped {
            memcpy(bytes.refTo(0), this@toByteArray.bytes, this@toByteArray.length)
        }
        return bytes
    }

    // Add this extension to get the top most view controller
    private fun UIViewController.Companion.topMostViewController(): UIViewController? {
        return findTopMostViewController(UIApplication.sharedApplication.keyWindow?.rootViewController)
    }

    private fun findTopMostViewController(rootViewController: UIViewController?): UIViewController? {
        if (rootViewController?.presentedViewController == null) {
            return rootViewController
        }

        if (rootViewController.presentedViewController is UINavigationController) {
            val navigationController =
                rootViewController.presentedViewController as UINavigationController
            return navigationController.visibleViewController ?: navigationController
        }

        if (rootViewController.presentedViewController is UITabBarController) {
            val tabBarController = rootViewController.presentedViewController as UITabBarController
            return tabBarController.selectedViewController ?: tabBarController
        }

        return null
    }
}
