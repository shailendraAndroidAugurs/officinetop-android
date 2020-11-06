package com.officinetop.officine.userprofile

import com.officinetop.officine.view_models.ListItemViewModel

data class ContactsInfo(var contactId: String? = null,
                        var displayName: String? = null,
                        var phoneNumber: String? = null) : ListItemViewModel(


)

