###############################################################################
"""
Date: 01/07/2008

Project: SPELL

Description
===========

Modifiers for SPELL functions and interfaces

Using Modifiers
===============
    
Notice that all configuration parameters (modifiers) can be passed in two ways::
    
    { Modifier:Value }
    
or::
        
    Modifier = Value
    
In the first case modifier names are written with leading capital letters
(e.g. ValueFormat) and they must be passed within a dictionary.
    
In the second case, modifiers are written separated by commas and the value is 
assigned with '='. 

Thus, the two next function calls are equivalent::

    Function( parameter, { Modifier:Value } )
    
or::    
    
    Function( parameter, Modifier = Value )
    
There are two constraints regarding the order at which the modifiers are written:

    1. Modifiers shall be passed as the last parameters of the function call
    2. Those modifiers passed using the second form SHALL be provided as the last ones.
    
For example, the following calls are valid::

    Function( parameter1, parameter2, Modifier2 = Value )
    Function( parameter1, parameter2, {Modifer1:Value}, Modifier2 = Value )
    
On the other hand, the following calls will fail::

    Function( parameter1, parameter2, Modifier2 = Value, {Modifer1:Value} )
    Function( Modifier2 = Value, parameter1, parameter2, {Modifer1:Value} )
    Function( {Modifer1:Value}, parameter1, parameter2, Modifier2 = Value )

Advanced Modifiers
==================

Three advanced modifiers can be used for very special cases.
    
    1. Notify:True/False        
    
        Enable/disable notifications to SPEL clients during the operation.
                            
    2. HandleError:True/False  
    
        Enable/disable exception catching in order to being able to process exceptions at proc level
                            
    3. GiveChoice:True/False   
    
        Give the user choice in case of failure.

    4. PromptUser:True/False   
    
        Prompt the user in case of failure or just let the function return a value. If the
        function returns a boolean value, the value returned can be overriden in case
        of failure by setting PromptUser to False and using OnFailure = SKIP or CANCEL.
        If SKIP is chosen, the return value will be True in case of failure. If
        CANCEL is chosen, the return value will be False in case of failure. If
        other action identifiers are chosen (e.g ABORT) the corresponding action
        will be carried out (e.g. abort the procedure immediately). 

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

#: Timeout for any SPELL operation. 
#:
#: Accepts any time in float format. Applicable to TM checks, command executions, 
#: wait functions, etc.
Timeout = 'Timeout'
#: Number of retries for TM comparisons. 
#:
#: Accepts an integer. 
#:
#: If a TM comparison fails, it will be repeated again as many times as specified 
#: by this modifier before actually failing.
Retries = 'Retries'
#: Wait flag for TM checks. 
#:
#: Accepts True/False.
#:
#: If True, the TM interface will wait for the next TM parameter update when
#: retrieving values. If False, the TM interface will provide the LRV (last
#: recorded value).
Wait = 'Wait'
#: Format of a TM value.
#:
#: Accepts one of the following SPELL constants: RAW,ENG,DEF.
#: 
#: Indicates the desired format of a TM parameter (uncalibrated or calibrated)
#: to be used in operations.
ValueFormat = 'ValueFormat'
#: Value type of a constant or variable.
#:
#: Accepts one of the following SPELL constants: STRING,LONG,FLOAT,DATETIME.
#: 
#: Indicates the desired format of a constant or variable.
ValueType = 'ValueType'
#: Radix of a constant or variable.
#:
#: Accepts one of the following SPELL constants: DEC,BIN,HEX,OCT.
#: 
#: Indicates the desired radix of a constant.Typically used in TC arguments
#: specifications.
Radix = 'Radix'
#: Units of a constant or variable.
#:
#: Accepts any string.
#: 
#: Indicates the desired units of a constant or variable. Typically used in
#: TC argument specifications.
Units = 'Units'
#: Notify clients flag.
#:
#: Accepts True/False.
#: 
#: Special modifier intended to be used in advanced operations. If True (default)
#: the SPELL interfaces will notify SPELL clients about the ongoing operation
#: status. If False, no notification will be done.
Notify = 'Notify'
#: Strict comparison flag.
#:
#: Accepts True/False.
#: 
#: Typically used in TM verification steps. Indicates wether the associated comparison
#: shall be strict or not.
Strict = 'Strict'
#: Try all flag.
#:
#: Accepts True/False.
#: 
#: When sending a group of commands (not a sequence) and one of them fails,
#: if this flag is False the whole operation will stop and fail. Otherwise, the rest
#: of the commands of the group will be sent.
TryAll = "TryAll"
#: On failure behavior specification.
#:
#: Accepts a combination of::
#:
#:      - ABORT
#:      - SKIP
#:      - CANCEL
#:      - REPEAT
#:      - RESEND
#:      - RECHECK
#:      - PROMPT
#:      - NOPROMPT
#:      - NOACTION
#: 
#: Combinations are made with the pipe '|' character. This specification determines
#: the list of choices (possible actions) shown to the user when a SPELL operation fails. 
#:
#: Notice that not all behavior constants are accepted by all SPELL functions. 
#: The SPELL function documentation indicate which constants are applicable in each case.
OnFailure = 'OnFailure'
#: Command block flag.
#:
#: Accepts True/False.
#: 
#: A group of commands (not a sequence) can be sent one by one or grouped in a
#: single frame. If this flag is True, the commands will be blocked. Otherwise
#: (default) they will be sent and verified independently.
Block = 'Block'
#: Sequence flag.
#:
#: Accepts True/False.
#: 
#: Intended to be used internally by SPELL interfaces, not for procedure developers.
#: Identifies a sequence and allows SPELL distinguishing it from a simple command.
Sequence = 'Sequence'
#: Host identifier.
#:
#: Accepts any string.
#: 
#: Specifies the host name to be used in a SPELL operation.
Host = 'Host'
#: Timestamp values.
#:
#: Accepts a TIME instance, a date-time string or a float value.
#: 
#: Used to pass dates and times to SPELL functions. Its specific meaning depends
#: on the particular SPELL function being used.
Time = 'Time'
#: Until time.
#:
#: Accepts a TIME instance, a date-time string or a float value.
#: 
#: Indicates a time line for waiting for it.
Until = 'Until'
#: Delay time.
#:
#: Accepts a TIME instance, a date-time string or a float value.
#: 
#: Indicates the amount of time to wait between two operations. For example,
#: in SendAndVerify function, it specifies the time to wait between the command
#: execution verification and the TM verifications.
Delay = 'Delay'
#: Delay time before command execution.
#:
#: Accepts a TIME instance, a date-time string or a float value.
#: 
#: Indicates the amount of time to wait before sending a command.
SendDelay = 'SendDelay'
#: Load command only, dont execute
#:
#: Accepts True/False
LoadOnly = 'LoadOnly'
#: Additional command information
#:
#: Accepts key/value strings
addInfo = 'addInfo'
#: Default value flag.
#:
#: If used as a modifier, provides the default value for a Prompt call.
#:
#: If used as a SPELL function argument value, it indicates that the SPELL
#: interfaces shall use the predefined default value for that argument.
Default = 'Default'
#: Message severity
#:
#: Accepts one of the following: INFORMATION, WARNING, ERROR or FATAL.
#: 
#: Used when injecting events into the controlled system or when displaying
#: messages.
Severity = 'Severity'
#: Event scope.
#:
#: Accepts one of the following: SCOPE_PROC, SCOPE_CFG or SCOPE_SYS.
#: 
#: Indicates the scope of an injected event.
Scope = 'Scope'
#: Operation mode.
#:
#: The accepted values depend on the SPELL function where it is used.
#: 
#: The semantics of this modifier depend on the SPELL function being used.
Mode = 'Mode'
#: Confirm flag.
#:
#: Accepts True/False.
#: 
#: Indicates if the corresponding SPELL operation shall be confirmed by the user
#: or not. For example, when used with Send() functions, it will prompt the
#: user for confirmation before sending the command/sequence.
Confirm ='Confirm'
#: Confirm flag.
#:
#: Accepts True/False.
#: 
#: Indicates if Send() functions must prompt the user for confirmation before
#: sending a command/sequence/group with critical commands.
ConfirmCritical ='ConfirmCritical'
#: Type indicator.
#:
#: The semantics and accepted values depend on the SPELL function being used. 
Type = 'Type'
#: Handle error flag.
#:
#: Accepts True/False.
#: 
#: Special modifier intended to be used in advanced operations. If true (default),
#: the SPELL function will manage any operation failure internally. If False,
#: any raisen exception will be forwarded to procedure level.
HandleError = 'HandleError'
#: Allow interrupt flag.
#:
#: Accepts True/False.
#: 
#: Special modifier intended to be used in advanced operations. If true,
#: the SPELL function can be interrupted in the middle of the operation.
AllowInterrupt = 'AllowInterrupt'
#: Give status flag.
#:
#: Accepts True/False.
#: 
#: Special modifier intended to be used in advanced operations. It has effect only
#: in case of operation failures. If true, the SPELL function will return a duple 
#: containing the function return value and the user choice after being prompted
#: for choosing an action. Default is False.
GiveChoice = 'GiveChoice'
#: Prompt user flag.
#:
#: Accepts True/False.
#: 
#: When True, the user will be prompted in case of true/false result. If False, the user
#: will not be prompted and the SPELL interface will take directly the action
#: specified by the OnTrue/OnFalse modifier.
PromptUser = 'PromptUser'
#: When True, the user will be prompted in case of failure. If False, the user
#: will not be prompted and the SPELL interface will take directly the action
#: specified by the OnFailure modifier.
#:
#: Accepts True/False.
#: 
PromptFailure = 'PromptFailure'
#: Retry flag.
#:
#: Accepts True/False.
#: 
#: Internal modifier not to be used in procedure developments.
Retry = 'Retry'
#: Adjust limits flag.
#:
#: Accepts True/False.
#: 
#: If True, the out of limits definitions of the associated TM parameter will
#: be adjusted using other given information. 
AdjLimits = 'AdjLimits'
#: On false behavior specification.
#:
#: Accepts one of the behavior constants (see OnFailure modifier).
#: 
#: If the SPELL operation has a boolean result and this result is False, the
#: given action will be taken automatically. Tipically the PROMPT/NOPROMPT
#: constants are used.
OnFalse = 'OnFalse'
#: On true behavior specification.
#:
#: Accepts one of the behavior constants (see OnFailure modifier).
#: 
#: If the SPELL operation has a boolean result and this result is True, the
#: given action will be taken automatically. Tipically the PROMPT/NOPROMPT
#: constants are used.
OnTrue = 'OnTrue'
#: On skip behavior specification.
#:
#: Accepts one of the behavior constants (see OnFailure modifier).
#: 
#: Indicates the function result when the user chooses SKIP in case of failure.
OnSkip = 'OnSkip'
#: Comparison tolerance.
#:
#: Accepts a float value.
#: 
#: Used in comparisons (TM verifications). Also used for adjusting TM parameter
#: limit definitions if no additional information is given.
Tolerance = 'Tolerance'
#: Low-Red Limit definition.
#:
#: Accepts a float value.
LoRed = 'LoRed'
#: Low-Yellow Limit definition.
#:
#: Accepts a float value.
LoYel = 'LoYel'
#: High-Red Limit definition.
#:
#: Accepts a float value.
HiRed = 'HiRed'
#: High-Yellow Limit definition.
#:
#: Accepts a float value.
HiYel = 'HiYel'
#: Middle point for limit definitions.
#:
#: Accepts a float value.
Midpoint = 'Midpoint'
#: Identifies the group of limits to be retrieved/changed
#:
#: Accepts a dictionary containing HiRed, HiYel, LoRed, LoYel.
Limits = 'Limits'
#: Identifies the group of expected status codes (status parameters)
#:
#: Accepts a comma separated list of string status codes
Expected = 'Expected'
#: String comparisons modifier
#:
#: If True, the string comparisons will be case insensitive
IgnoreCase = 'IgnoreCase'
#: Both yellow/red low limit definition.
#:
#: Accepts a float value.
LoBoth = 'LoBoth'
#: Both yellow/red high limit definition.
#:
#: Accepts a float value.
HiBoth = 'HiBoth'
#: Interval for WaitFor warnings
#:
#: Accepts a list of time objects/floats/integers.
Interval = 'Interval'
#: Interval for WaitFor warnings
#:
#: Accepts a list of time objects/floats/integers.
Message = 'Message'
#: Printer name to print a display
#:
#: Accepts any string
Printer = 'Printer'
#: Format to export a display
#:
#: Accepts any string
Format = 'Format'
#: Internal use: enable/disable runinto feature
#:
#: Accepts True/False
RunInto = 'RunInto'
#: Internal use: enable/disable step-by-step feature
#:
#: Accepts True/False
ByStep = 'ByStep'
#: Internal use: execution delay time
#:
#: Accepts <TIME>
ExecDelay = 'ExecDelay'
#: Procedure start mode: if True, procedure will run once loaded
#:
#: Accepts True/False
Automatic = 'Automatic'
#: Procedure start mode: if True, procedure will appear on clients
#:
#: Accepts True/False
Visible = 'Visible'
#: Procedure start mode: if True, procedure will block the parent procedure
#:
#: Accepts True/False
Blocking = 'Blocking'
#: Message verbosity level
#:
#: Accepts 0 to 10
Verbosity = 'Verbosity'
#ReleaseTime (release commands at exact time)
ReleaseTime = 'ReleaseTime'
