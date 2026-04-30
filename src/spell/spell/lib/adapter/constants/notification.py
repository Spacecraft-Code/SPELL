###################################################################################
## MODULE     : spell.lib.adapter.constants.notification
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: Notification data
## --------------------------------------------------------------------------------
##
##  Copyright (C) 2008, 2018 SES ENGINEERING, Luxembourg S.A.R.L.
##
##  This file is part of SPELL.
##
## This component is free software: you can redistribute it and/or
## modify it under the terms of the GNU Lesser General Public
## License as published by the Free Software Foundation, either
## version 3 of the License, or (at your option) any later version.
##
## This software is distributed in the hope that it will be useful,
## but WITHOUT ANY WARRANTY; without even the implied warranty of
## MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
## Lesser General Public License for more details.
##
## You should have received a copy of the GNU Lesser General Public
## License and GNU General Public License (to which the GNU Lesser
## General Public License refers) along with this library.
## If not, see <http://www.gnu.org/licenses/>.
##
###################################################################################


###############################################################################
# UI notification fields
NOTIF_PROC_ID     = "ProcId"
NOTIF_SUBPROC_ID  = "SubProcId"
NOTIF_LINE        = "Line"
NOTIF_DATA_TYPE   = "DataType"
NOTIF_ITEM_TYPE   = "ItemType"
NOTIF_ITEM_NAME   = "ItemName"
NOTIF_ITEM_VALUE  = "ItemValue"
NOTIF_ITEM_STATUS = "ItemStatus"
NOTIF_ITEM_REASON = "ItemReason"
NOTIF_ITEM_TIME   = "ItemTime"
NOTIF_ITEM_EOS    = "EndOfScript"
NOTIF_STATUS      = "Status"

###############################################################################
# UI notification type values
DATA_TYPE_NOTIF  = 'ITEM'
NOTIF_TYPE_VAL   = 'VALUE'
NOTIF_TYPE_VERIF = 'VERIFICATION'
NOTIF_TYPE_EXEC  = 'EXECUTION'
NOTIF_TYPE_SYS   = 'SYSTEM'
NOTIF_TYPE_TIME  = 'TIME'

###############################################################################
# UI notification status values
NOTIF_STATUS_OK = 'SUCCESS'
NOTIF_STATUS_PR = 'IN PROGRESS'
NOTIF_STATUS_FL  = 'FAILED'
NOTIF_STATUS_SP  = 'SKIPPED'
NOTIF_STATUS_CL  = 'CANCELLED'

###############################################################################
# Notification description texts
OPERATION_SUCCESS = "Success"
OPERATION_FAILED  = "Failed"
OPERATION_SKIPPED = "Skipped"
OPERATION_REPEAT  = "Repeat"
OPERATION_PROGRESS = "In Progress"
OPERATION_CANCELLED = "Cancelled"
OPERATION_HANDLED = "Handled"

###############################################################################
# Item separator
ITEM_SEP = ",,"
