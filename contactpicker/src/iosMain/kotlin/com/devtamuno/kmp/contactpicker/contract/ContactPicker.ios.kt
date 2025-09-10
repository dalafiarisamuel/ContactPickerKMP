package com.devtamuno.kmp.contactpicker.contract

import androidx.compose.runtime.Composable
import com.devtamuno.kmp.contactpicker.data.Contact
import platform.Contacts.CNContact
import platform.Contacts.CNContactPhoneNumbersKey
import platform.Contacts.CNLabeledValue
import platform.Contacts.CNPhoneNumber
import platform.ContactsUI.CNContactPickerDelegateProtocol
import platform.ContactsUI.CNContactPickerViewController
import platform.Foundation.NSString
import platform.UIKit.UIApplication
import platform.UIKit.UINavigationController
import platform.UIKit.UITabBarController
import platform.UIKit.UIViewController
import platform.darwin.NSObject

internal actual class ContactPicker {

    private val contactPicker = CNContactPickerViewController()

    private lateinit var onContactSelected: (Contact?) -> Unit

    @Composable
    actual fun registerContactPicker(onContactSelected: (Contact?) -> Unit) {
        this.onContactSelected = onContactSelected
    }

    actual fun launchContactPicker() {
        contactPicker.delegate = object : NSObject(), CNContactPickerDelegateProtocol {
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

                onContactSelected(Contact(id, name, phoneNumbers, email))
                contactPicker.delegate = null
                picker.dismissViewControllerAnimated(true, null)

            }
        }
        contactPicker.setDisplayedPropertyKeys(listOf(CNContactPhoneNumbersKey))

        UIViewController.topMostViewController()?.presentViewController(contactPicker, true, null)
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
