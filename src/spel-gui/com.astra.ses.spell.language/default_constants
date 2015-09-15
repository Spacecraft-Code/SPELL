###############################################################################
"""
Date: 01/07/2008

Project: SPELL

Description
===========

Constants for procedures and SPELL functions

Authoring
=========

@organization: SES ENGINEERING

@copyright: Copyright (C) 2008, 2010 SES ENGINEERING, Luxembourg S.A.R.L.
            
@license:  This file is part of SPELL.

 This library is free software: you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation, either
 version 3 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License and GNU General Public License (to which the GNU Lesser
 General Public License refers) along with this library.
 If not, see <http://www.gnu.org/licenses/>.
 
@version: 1.0
@requires: Python 2.5.x
"""
###############################################################################


###############################################################################
#: TM parameter value format RAW (uncalibrated). To be used tipically with ValueFormat 
#: modifier in all telemetry-related SPELL functions.
RAW = "Raw format" 
#: TM parameter value format ENG (calibrated). To be used tipically with ValueFormat 
#: modifier in all telemetry-related SPELL functions.
ENG = "Eng format"
#: TM parameter value format DEF (default). To be used tipically with ValueFormat 
#: modifier in all telemetry-related SPELL functions.
DEF = "Default format"

###############################################################################
#: Failure behavior ABORT. When a failure occurs inside a SPELL function call,
#: the user will be prompted to decide what to do after the failure. The set
#: of options that will be shown to the user is determined by the OnFailure
#: modifier whose value must be a combination of behavior constants like this.
#: Behaviors are combined by using the pipe '|' character. 
#:
#: This behavior means that the procedure execution shall be immediately aborted.
ABORT    = 1  
#: Failure behavior CANCEL. When a failure occurs inside a SPELL function call,
#: the user will be prompted to decide what to do after the failure. The set
#: of options that will be shown to the user is determined by the OnFailure
#: modifier whose value must be a combination of behavior constants like this.
#: Behaviors are combined by using the pipe '|' character. 
#:
#: This behavior means that the SPELL function shall be cancelled and the value
#: False shall be returned.
CANCEL   = 2  
#: Failure behavior SKIP. When a failure occurs inside a SPELL function call,
#: the user will be prompted to decide what to do after the failure. The set
#: of options that will be shown to the user is determined by the OnFailure
#: modifier whose value must be a combination of behavior constants like this.
#: Behaviors are combined by using the pipe '|' character. 
#:
#: This behavior means that the SPELL function shall be cancelled and the value
#: True shall be returned.
SKIP     = 4  # Ask the user if the check should be skipped
#: Failure behavior RECHECK. When a failure occurs inside a SPELL function call,
#: the user will be prompted to decide what to do after the failure. The set
#: of options that will be shown to the user is determined by the OnFailure
#: modifier whose value must be a combination of behavior constants like this.
#: Behaviors are combined by using the pipe '|' character. 
#:
#: Applicable to SendAndVerify function only. It means that the TM verification
#: should be repeated.
RECHECK  = 8  # Ask the user if the TM param should be rechecked
#: Failure behavior RESEND. When a failure occurs inside a SPELL function call,
#: the user will be prompted to decide what to do after the failure. The set
#: of options that will be shown to the user is determined by the OnFailure
#: modifier whose value must be a combination of behavior constants like this.
#: Behaviors are combined by using the pipe '|' character. 
#:
#: Applicable to telecommand-related functions like Send or SendAndVerify. This 
#: behavior means that the command shall be sent again.
RESEND   = 16 # Try resending command
#: Failure behavior REPEAT. When a failure occurs inside a SPELL function call,
#: the user will be prompted to decide what to do after the failure. The set
#: of options that will be shown to the user is determined by the OnFailure
#: modifier whose value must be a combination of behavior constants like this.
#: Behaviors are combined by using the pipe '|' character. 
#:
#: This behavior means that the SPELL function call shall be retried.
REPEAT   = 32 # Repeat generic operation
#: Failure behavior NOACTION. When a failure occurs inside a SPELL function call,
#: the user will be prompted to decide what to do after the failure. The set
#: of options that will be shown to the user is determined by the OnFailure
#: modifier whose value must be a combination of behavior constants like this.
#: Behaviors are combined by using the pipe '|' character. 
#:
#: Special behavior not intended to be used in procedure developments. 
NOACTION = 64 # No behavior
#: Failure behavior PROMPT. When a failure occurs inside a SPELL function call,
#: the user will be prompted to decide what to do after the failure. The set
#: of options that will be shown to the user is determined by the OnFailure
#: modifier whose value must be a combination of behavior constants like this.
#: Behaviors are combined by using the pipe '|' character. 
#:
#: This behavior indicates that the user shall be prompted in case of failure.
#: This is the default behavior of all SPELL functions.
PROMPT   = 128
#: Failure behavior NOPROMPT. When a failure occurs inside a SPELL function call,
#: the user will be prompted to decide what to do after the failure. The set
#: of options that will be shown to the user is determined by the OnFailure
#: modifier whose value must be a combination of behavior constants like this.
#: Behaviors are combined by using the pipe '|' character. 
#:
#: This behavior indicates that the user shall not be prompted in case of failure.
#: On the contrary, the action specified in the OnFailure modifier shall be
#: directly choosen. It is expected that OnFailure contains a single behavior
#: constant when using NOPROMPT.
NOPROMPT = 256# Do not prompt the user

###############################################################################
#: 'Equal-To' comparison identifier.
eq = "equal to"
#: 'Not-Equal-To' comparison identifier.
neq = "not equal to"
#: 'Less-Than' comparison identifier.
lt = "less than"
#: 'Greater-Than' comparison identifier.
gt = "greater than"
#: 'Less-Or-Equal-To' comparison identifier.
le = "less or equal than"
#: 'Greater-Or-Equal-To' comparison identifier.
ge = "greater or equal to"
#: 'Between-Values' comparison identifier.
bw = "between"
#: 'Not-Between-Values' comparison identifier.
nbw = "not between"

###############################################################################
#: Event scope indicating procedure level. Typically used when injecting events
#: in relation to the procedure logic. Used with Scope modifier.            
SCOPE_PROC = 1
#: Event scope indicating system level. Typically used when injecting events
#: in relation to the controlled system status. Used with Scope modifier.            
SCOPE_SYS  = 2
#: Event scope indicating configuration level. Typically used when injecting events
#: in relation to SPELL configuration. Used with Scope modifier.
SCOPE_CFG  = 4

###############################################################################
#: Event and message severity: warnings. Used with Severity modifier.        
WARNING     = 1
#: Event and message severity: errors. Used with Severity modifier.      
ERROR       = 2
#: Event and message severity: fatal errors. Used with Severity modifier.          
FATAL       = 4
#: Event and message severity: information messages. Used with Severity modifier.         
INFORMATION = 8

###############################################################################
#: Value radix decimal. Used with Radix modifier.            
DEC = 'dec'
#: Value radix hexadecimal. Used with Radix modifier.            
HEX = 'hex'
#: Value radix octal. Used with Radix modifier.
OCT = 'oct'
#: Value radix binary. Used with Radix modifier.
BIN = 'bin'

###############################################################################
#: Value type long. Used with ValueType modifier.
LONG     = 'long'
#: Value type float. Used with ValueType modifier.
FLOAT    = 'float'
#: Value type boolean. Used with ValueType modifier.
BOOLEAN  = 'bool'
#: Value type string. Used with ValueType modifier.
STRING   = 'string'
#: Value type date/time. Used with ValueType modifier.
DATETIME = 'datetime'

###############################################################################
#: Message type for normal display. Used with Type modifier.
DISPLAY  = 'DISPLAY'
#: Message type for log view display. Used with Type modifier.
LOGVIEW  = 'LOG'
#: Message type for dialogs. Used with Type modifier.
DIALOG   = 'DIALOG'

###############################################################################
#: Prompt option type for accepting OK only as an answer. Used with Type modifier 
#: in Prompt() function.
OK        = 1
#: Prompt option type for accepting CANCEL only as an answer. Used with Type modifier 
#: in Prompt() function.
CANCEL    = 2
#: Prompt option type for accepting YES only as an answer. Used with Type modifier 
#: in Prompt() function.
YES       = 4
#: Prompt option type for accepting NO only as an answer. Used with Type modifier 
#: in Prompt() function.
NO        = 8
#: Prompt option type for accepting numeric answers only. Used with Type modifier 
#: in Prompt() function.
NUM       = 16
#: Prompt option type for accepting any alphanumeric answer. Used with Type modifier 
#: in Prompt() function.
ALPHA     = 32
#: Prompt option type for specifying lists of choices. Used with Type modifier 
#: in Prompt() function.
LIST      = 64
#: Prompt option type for accepting YES or NO as an answer. Used with Type modifier 
#: in Prompt() function.
YES_NO    = 128
#: Prompt option type for accepting OK or CANCEL as an answer. Used with Type modifier 
#: in Prompt() function.
OK_CANCEL = 512
#: Prompt option type for accepting a date/time string as an answer. Used with Type modifier 
#: in Prompt() function.
DATE      = 1024
#: Prompt option type for list with drop-down list 
#: in Prompt() function.
COMBO     = 2048

###############################################################################
# Time access modes
#: Access mode live when retrieving telemetry, commands or events information.
TIME_MODE_LIVE = 'live mode'
#: Access mode retrieval (forwards) when retrieving telemetry, commands or events information.
TIME_MODE_FWD = 'forwards retrieval mode'
#: Access mode retrieval (backwards) when retrieving telemetry, commands or events information.
TIME_MODE_BWD = 'backwards retrieval mode'

################################################################################
# User action identifiers.
ACTION_ABORT = 'A'
ACTION_REPEAT = 'R'
ACTION_RESEND = 'S'
ACTION_RECHECK = 'C'
ACTION_SKIP = 'K'
ACTION_NOACTION = 'N'
ACTION_CANCEL = 'Q'
ACTION_CANCEL_PMT = 'P'
