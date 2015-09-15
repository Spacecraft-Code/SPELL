################################################################################
#
# LIBRARY     : user_functions
# DESCRIPTION : contains utility functions used in procedures 
#
# DEVELOPED   : <author> 
# VERIFIED    : <author>
# VALIDATED   : <author>
#
# REVISION HISTORY:
#
# DATE          REV   AUTHOR      DESCRIPTION
# ===========   ===   =========   ==============================================
# 22 Mar 2011   0.1   <author>    Initial release
#
################################################################################
#
# <license information>
#
################################################################################

#===============================================================================
def ProcedureStart():
    '''
    Notifies the start of the procedure operations and confirms execution
    '''
    if not Prompt('Do you really want to execute this procedure?', YES_NO):
        Finish('Procedure dismissed')
    #ENDIF
    Event('Procedure Example RLG started', Severity = WARNING )
    Display('Procedure started at ' + str(NOW))
    return

#===============================================================================
def ProcedureEnd():
    '''
    Notifies the end of the procedure operations
    '''
    Event('Procedure Example RLG finished', Severity = WARNING )
    Finish('Procedure finished')
    return

#===============================================================================
def Progress( message ):
    '''
    Notifies a progress in the procedure operations
    '''
    Event( message)
    Display( message )
    return
    
#===============================================================================
def Failure( message, shouldAbort = False ):
    '''
    Notifies a failure in the procedure operations
    '''
    Event('Procedure Example RLG failed', Severity = ERROR )
    if shouldAbort:
        Abort(message)
    else:
        Display(message, Severity = ERROR)
    #ENDIF
    return


