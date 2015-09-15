################################################################################
#
# NAME          : EQU Example
# DESCRIPTION   : This procedure simulates the configuration of some imaginary
#                 equipment unit 'EQU' on board.
#
# SPACECRAFT: STD
#
# SPECIFICATION : Based on paper procedure 'XYZ'
#
# CATEGORY  : Examples
#
# DEVELOPED : <author>
# VERIFIED  : <author>
# VALIDATED : <author>
#
# REVISION HISTORY:
#
# DATE          REV   AUTHOR      DESCRIPTION
# ===========   ===   =========   ==============================================
# 22-MAR-2011   0.1   <author>    Initial release
#
################################################################################
#
# <license information>
#
################################################################################

#===============================================================================
def TurnON_EQU(rlg):
    '''
    Turn on the given equipment unit. Two units are available, EQU1 and EQU2.
   
        - The command to turn on each unit are different
        - TM verification is performed to ensure the correctness of the
          command execution.
        
    '''

    # Will hold the result of the Send operation
    result = False

    # Depending on the EQU chosen
    if rlg == 'EQU1':
        result = Send(command = 'C CMD0001 Descr', 
                      verify  = [ [ 'T TM0001 Descr', eq, 'ON' ],
                                  [ 'T TM0002 Descr', eq, 'ON' ] ], 
                      Retries = 10,
                      Wait = False)
    elif rlg == 'EQU2':
        result = Send(command = 'C CMD0002 Descr', 
                      verify  = [ [ 'T TM0003 Descr', eq, 'ON' ],
                                  [ 'T TM0004 Descr', eq, 'ON' ] ], 
                      Retries = 10,
                      Wait = False)
    else:
        Failure('Unknown EQU given: ' + repr(rlg))
    #ENDIF
    return result     

#=============================================================================== 
def Is_EQU_Data_Valid(rlg):
    '''
    Check the EQU data correctness. Once switched on, the spacecraft sends
    status information through the telemetry stream.
    '''
    # build tm verification list 
    if rlg == 'EQU1':
        EQU_DATA_VALIDITY_TM = [ [ 'T TM0005 EQU1 Status 1',  eq, 'Go'       ],
                                 [ 'T TM0006 EQU1 Status 2',  eq, 'Valid'    ],
                                 [ 'T TM0007 EQU1 Status 3',  eq, 'Pass'     ],
                                 [ 'T TM0008 EQU1 Status 4',  eq, 'OK'       ]]
    elif rlg == 'EQU2':
        EQU_DATA_VALIDITY_TM = [ [ 'T TM0009 EQU2 Status 1',  eq, 'Go'       ],
                                 [ 'T TM0010 EQU2 Status 2',  eq, 'Valid'    ],
                                 [ 'T TM0011 EQU2 Status 3',  eq, 'Pass'     ],
                                 [ 'T TM0012 EQU2 Status 3',  eq, 'OK'       ]]
    #ENDIF

    # Verify telemetry per previous tm list. Return True if EQU data is valid 
    # otherwise False.    
    # In case of false condition, the user is prompted with some choices,
    # 
    #    - Cancel the verification (the function returns False but the execution
    #      continues)
    #
    #    - Abort the procedure
    #
    #    - Retry the verification 
    #
    # Before declaring any check as failed, each parameter value is checked 5 
    # times (each time with a new sample). This is set by Retries modifier.  
    return Verify(EQU_DATA_VALIDITY_TM, 
                  PromptUser = True, 
                  OnFalse = CANCEL|ABORT|RECHECK, 
                  Retries=5)

#=============================================================================== 
def Display_EQU_Data(rlg):
    '''
    Display the EQU data for the user.
    '''
    # build data list
    data = {}
    # display header
    Display('===================================')
    Display('Current status of ' + str(rlg) + ':') 
    if rlg == 'EQU1':
        data['Status 1'] = GetTM('T TM0005 EQU1 Status 1',) 
        data['Status 2'] = GetTM('T TM0006 EQU1 Status 2',) 
        data['Status 3'] = GetTM('T TM0007 EQU1 Status 3' ) 
        data['Status 4'] = GetTM('T TM0008 EQU1 Status 4' )
    elif rlg == 'EQU2':
        data['Status 1'] = GetTM('T TM0009 EQU1 Status 1',) 
        data['Status 2'] = GetTM('T TM0010 EQU1 Status 2',) 
        data['Status 3'] = GetTM('T TM0011 EQU1 Status 3' ) 
        data['Status 4'] = GetTM('T TM0012 EQU1 Status 4' ) 
    #ENDIF

    # iterate over the dictionary to display the data
    for item in data.keys():
        Display('   - %s : %5s' % (item, data[item]))
    #ENDFOR

    Display('===================================')
    return 

#=============================================================================== 
def Configure_EQU(rlg):
    '''
    Select a given EQU for control
    '''

    # Will hold the result of the Send operation
    result = False
    
    # Depending on the EQU
    if rlg == 'EQU1':
        result = Send(command = 'C CMD0003 Descr', verify  = [ [ 'T TM0040 Descr', eq, 'EQU1' ] ])
    elif rlg == 'EQU2':
        result = Send(command = 'C CMD0004 Descr', verify  = [ [ 'T TM0040 Descr', eq, 'EQU2' ] ])
    else:
        Failure('Unknown EQU given: ' + repr(rlg))
    #ENDIF
    
    # Wait for 5 seconds 
    WaitFor( 5 * SECOND )
        
    return result         

#===============================================================================            
def Select_EQU(rlg):
    '''
    Prepare the given EQU unit.
    '''

    # Will hold the result of the Send operation
    result = False
    
    # Depending on the EQU
    if rlg == 'EQU1':
        result = Send(command = 'C CMD0005 Descr', verify  = [ [ 'T TM0050 Descr', eq, 'EQU1' ] ])
    elif rlg == 'EQU2':
        result = Send(command = 'C CMD0006 Descr', verify  = [ [ 'T TM0050 Descr', eq, 'EQU2' ] ])
    else:
        Failure('Unknown EQU given: ' + repr(rlg))
    #ENDIF
    return result

#=============================================================================== 
def EQU_For_Control(rlg):
    '''
    Set the given EQU for control
    '''

    # Will hold the result of the Send operation
    result = False
    
    # Depending on the EQU
    if rlg == 'EQU1':
        result = Send(command = 'C CMD0007 Descr', verify  = [ [ 'T TM0060 Descr', eq, 'EQU1' ] ])
    elif rlg == 'EQU2':
        result = Send(command = 'C CMD0008 Descr', verify  = [ [ 'T TM0060 Descr', eq, 'EQU2' ] ])
    else:
        Failure('Unknown EQU given: ' + repr(rlg))
    #ENDIF
    return result


################################################################################
#                                                                              #
#                        MAIN PROCEDURE CODE                                   #
#                                                                              #
################################################################################

################################################################################

Step('INIT', 'Preamble')

# The procedure is starting (see the user library)
ProcedureStart()

# Preconfigure some function defaults (overrides the system config)
ChangeLanguageConfig( GetTM, Timeout = 10 * SECOND )
ChangeLanguageConfig( GetTM, Wait = False )
ChangeLanguageConfig( Verify, Wait = False )

# Disable some kind of EQU monitor mechanism on the GCS, when needed
usingMonitor = GetResource('EQU_MONITOR') == 'ENABLED'
if usingMonitor: SetResource('EQU_MONITOR', 'DISABLED')


################################################################################
Step('1','Which EQU is to be turned ON?')
################################################################################

# Select which EQU is going to be used. Gets from the Spacecraft database the
# list of EQUs available in the spacecraft.
selected_EQU = Prompt("Select EQU", SCDB['Available_EQUs'], Type=LIST|ALPHA )

# Holds the operation success flag
operationSuccess = False

# Procedure branch. Althoug simpler code could be written without this IF clause,
# it is strongly recommended to try to resemble the paper procedure structure
# so that there is a one-to-one correspondence with the electronic procedure.
if selected_EQU == 'EQU1':
    
    ###########################################################################
    Step('2','Turn on EQU1')
    ###########################################################################

    # Notify the operation
    Progress("Turning ON EQU1")

    # We will keep doing the operation in case of failure
    operationFinished = False
    #--------------------------------------------------------------------------
    while not operationFinished:

        #----------------------------------------------------------------------
        Step('2.1','Select EQU1')
        Select_EQU('EQU1')
        #----------------------------------------------------------------------
    
        #----------------------------------------------------------------------
        Step('2.2','Turn ON EQU1')
        operationSuccess = TurnON_EQU('EQU1') 
        #----------------------------------------------------------------------
        if not operationSuccess: 
            Failure('EQU1 did not turn ON properly.') 
        else:
            #------------------------------------------------------------------
            # If it was correctly turned on, then check the configuration
            Step('2.3','Check EQU1')
            Progress('EQU1 successfully turned ON.')
            dataValid = Is_EQU_Data_Valid('EQU1')

            #------------------------------------------------------------------
            if not dataValid:
                Display_EQU_Data('EQU1')
                Display('EQU1 data is not valid', Severity = WARNING )
                if Prompt("Shall try to reconfigure EQU?"):
                    Step('2.4','Configure EQU1')
                    operationSuccess = Configure_EQU('EQU1')
                    # If configuration failed, notify
                    if not operationSuccess:
                        Failure('Failed to configure EQU1')
                    else:
                        Progress('EQU1 configured.')
                    # Check again the EQU configuration
                    Goto('2.3')
                else:
                    Display('EQU configuration left incorrect', Severity = WARNING )
                    operationSuccess = False
                #ENDIF operationSuccess
            else:
                Progress('EQU1 configuration is correct.')
            #ENDIF dataValid --------------------------------------------------
            Step('2.5','EQU1 configuration finished')
    
        #ENDIF turnOn ---------------------------------------------------------
        
        #----------------------------------------------------------------------
        if not operationSuccess:
            operationFinished = not Prompt('EQU1 setup failed. Should retry operation?', YES_NO)
        else:
            operationFinished = True
        #ENDIF operationSuccess
        
    #ENWHILE operationFinished ------------------------------------------------

    ###########################################################################

elif selected_EQU == 'EQU2':
    
    ###########################################################################
    Step('3','Turn on EQU2')
    ###########################################################################

    # Notify the operation
    Progress("Turning ON EQU2")

    # We will keep doing the operation in case of failure
    operationFinished = False
    #--------------------------------------------------------------------------
    while not operationFinished:

        #----------------------------------------------------------------------
        Step('3.1','Select EQU2')
        Select_EQU('EQU2')
        #----------------------------------------------------------------------
    
        #----------------------------------------------------------------------
        Step('3.2','Turn ON EQU2')
        operationSuccess = TurnON_EQU('EQU2') 
        #----------------------------------------------------------------------
        if not operationSuccess: 
            Failure('EQU2 did not turn ON properly.') 
        else:
            #------------------------------------------------------------------
            # If it was correctly turned on, then check the configuration
            Step('3.3','Check EQU2')
            Progress('EQU2 successfully turned ON.')
            dataValid = Is_EQU_Data_Valid('EQU2')

            #------------------------------------------------------------------
            if not dataValid:
                Display_EQU_Data('EQU2')
                Display('EQU2 data is not valid', Severity = WARNING )
                if Prompt("Shall try to reconfigure EQU?"):
                    Step('3.4','Configure EQU2')
                    operationSuccess = Configure_EQU('EQU2')
                    # If configuration failed, notify
                    if not operationSuccess:
                        Failure('Failed to configure EQU2')
                    else:
                        Progress('EQU2 configured.')
                    # Check again the EQU configuration
                    Goto('3.3')
                else:
                    Display('EQU configuration left incorrect', Severity = WARNING )
                    operationSuccess = False
                #ENDIF operationSuccess
            else:
                Progress('EQU2 configuration is correct.')
            #ENDIF dataValid --------------------------------------------------
            Step('3.5','EQU2 configuration finished')
    
        #ENDIF turnOn ---------------------------------------------------------
        
        #----------------------------------------------------------------------
        if not operationSuccess:
            operationFinished = not Prompt('EQU2 setup failed. Should retry operation?', YES_NO)
        else:
            operationFinished = True
        #ENDIF operationSuccess
        
    #ENWHILE operationFinished ------------------------------------------------

    ###########################################################################

#ENDIF

################################################################################
Step('4','EQU Configuration finished, select EQU for control')
################################################################################
EQU_For_Control(selected_EQU)

################################################################################
Step('5','Cleanup')
################################################################################
# Store the result of the operation on a ground telemetry parameter readable by GCS
if operationSuccess:
    SetGroundParameter('T TMFLAG Ground parameter holds the result', 1)
else:
    SetGroundParameter('T TMFLAG Ground parameter holds the result', 2)

# Enable some kind of EQU monitor mechanism on the GCS, when needed
if usingMonitor: SetResource('EQU_MONITOR', 'ENABLED')
        
################################################################################
# Notify the end
Step('END','End of procedure')
ProcedureEnd()

