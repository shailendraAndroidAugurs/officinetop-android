package com.officinetop.userprofile

import com.officinetop.view_models.ListItemViewModel

data class ContactsInfo(var contactId: String? = null,
                        var displayName: String? = null,
                        var phoneNumber: String? = null) : ListItemViewModel(


)

