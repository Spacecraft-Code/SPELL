// ################################################################################
// FILE       : SPELLcifHelper.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the CIF helper
// --------------------------------------------------------------------------------
//
//  Copyright (C) 2008, 2015 SES ENGINEERING, Luxembourg S.A.R.L.
//
//  This file is part of SPELL.
//
// SPELL is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// SPELL is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with SPELL. If not, see <http://www.gnu.org/licenses/>.
//
// ################################################################################

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// System includes ---------------------------------------------------------
// Local includes ----------------------------------------------------------
#include "SPELL_CIF/SPELLcifHelper.H"
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLpyArgs.H"
#include "SPELL_WRP/SPELLconstants.H"
#include "SPELL_CFG/SPELLconfiguration.H"
#include "SPELL_EXC/SPELLexecutor.H"



// DEFINES /////////////////////////////////////////////////////////////////
// GLOBALS /////////////////////////////////////////////////////////////////
// STATIC //////////////////////////////////////////////////////////////////

//============================================================================
// METHOD:    SPELLcifHelper::strToBoolean()
//============================================================================
bool SPELLcifHelper::strToBoolean( std::string str )
{
    if (str == "TRUE" || str == "True" || str == "Y" || str == "O" ) return true;
    return false;
}

//=============================================================================
// METHOD    : SPELLcifHelper::generatePromptOptions()
//=============================================================================
void SPELLcifHelper::generatePromptOptions( PyObject* args, SPELLpromptDefinition& def )
{
    SPELLpyArgs argumentsA(args);

    PyObject* optionsObj = argumentsA[1];
    PyObject* configObj = argumentsA[2];

	def.defaultAnswer = "";

    SPELLpyArgs argumentsC(args,configObj);

    std::string deflt = "";
	if (argumentsC.hasModifier(LanguageModifiers::Default))
	{
		deflt = argumentsC.getModifier_Default();
	}

    if ((optionsObj != NULL)&&(PyList_Size(optionsObj)>0))
    {
        LOG_INFO("Using options object");

    	int listSize = PyList_Size(optionsObj);
    	DEBUG("[CIF PY] Number of prompt options: " + ISTR(listSize))
    	for(int keyIndex=0; keyIndex<listSize; keyIndex++)
    	{
    		PyObject* item = PyList_GetItem(optionsObj, keyIndex);
    		std::string optionString = PYSTR(item);

    		std::string key = "";

    		// ALPHA option
    		bool alpha = false;
    		if (argumentsC.hasModifier(LanguageModifiers::Type))
    		{
    			int type = PyLong_AsLong(argumentsC[LanguageModifiers::Type]);
    			alpha = (( type & LanguageConstants::PROMPT_ALPHA ) > 0 && (type & LanguageConstants::PROMPT_LIST) > 0);
    		}

    		if (alpha)
    		{
				if ( optionString.find(":") == std::string::npos )
				{
					key = optionString;
				}
				else
				{
					// If in alhpa and either way a colon is provided, remove it
					int idx = optionString.find(":");
					optionString = optionString.substr(idx+1, optionString.size()-idx);
					SPELLutils::trim(optionString);
					key = optionString;
				}
    		}
    		else
    		{
				if ( optionString.find(":") == std::string::npos )
				{
					// Put an internal key in this case
					key = ISTR(keyIndex+1);
				}
				else
				{
					int idx = optionString.find(":");
					key = optionString.substr(0, idx);
					// Trim the key
					SPELLutils::trim(key);
				}
    		}
    		def.options.push_back( optionString );
            def.expected.push_back(key);
    	}

    	if (deflt != "")
    	{
    		std::vector<std::string>::iterator it = std::find(def.expected.begin(),def.expected.end(),deflt);
    		if (it != def.expected.end())
    		{
    			def.defaultAnswer = deflt;
    		}
    	}
    }
    else
    {
        LOG_INFO("TYPECODE " + ISTR(def.typecode));
        DEBUG("[CIF] Prompt typecode " + ISTR(def.typecode));
    	switch(def.typecode)
    	{
			case LanguageConstants::PROMPT_OK:
			{
				def.options.push_back("O: Ok");
				def.expected.push_back("O");
				if (deflt == "O" || deflt == ISTR(LanguageConstants::PROMPT_OK))
				{
					def.defaultAnswer = "O";
				}
				break;
			}
			case LanguageConstants::PROMPT_CANCEL:
			{
				def.options.push_back("C: Cancel");
				def.expected.push_back("C");
				if (deflt == "C" || deflt == ISTR(LanguageConstants::PROMPT_CANCEL))
				{
					def.defaultAnswer = "C";
				}
				break;
			}
			case LanguageConstants::PROMPT_YES:
			{
				def.options.push_back("Y: Yes");
				def.expected.push_back("Y");
				if (deflt == "Y" || deflt == ISTR(LanguageConstants::PROMPT_YES))
				{
					def.defaultAnswer = "Y";
				}
				break;
			}
			case LanguageConstants::PROMPT_NO:
			{
				def.options.push_back("N: No");
				def.expected.push_back("N");
				if (deflt == "N" || deflt == ISTR(LanguageConstants::PROMPT_NO))
				{
					def.defaultAnswer = "N";
				}
				break;
			}
			case LanguageConstants::PROMPT_YES_NO:
			{
				def.options.push_back("Y: Yes");
				def.options.push_back("N: No");
				def.expected.push_back("Y");
				def.expected.push_back("N");
				if (deflt == "Y" || deflt == ISTR(LanguageConstants::PROMPT_YES) )
				{
					def.defaultAnswer = "Y";
				}
				else if (deflt == "N" || deflt == ISTR(LanguageConstants::PROMPT_NO))
				{
					def.defaultAnswer = "N";
				}
				break;
			}
			case LanguageConstants::PROMPT_OK_CANCEL:
			{
				def.options.push_back("O: Ok");
				def.options.push_back("C: Cancel");
				def.expected.push_back("O");
				def.expected.push_back("C");
				if (deflt == "O" || deflt == ISTR(LanguageConstants::PROMPT_OK) )
				{
					def.defaultAnswer = "O";
				}
				else if (deflt == "C" || deflt == ISTR(LanguageConstants::PROMPT_CANCEL))
				{
					def.defaultAnswer = "C";
				}
				break;
			}
			// For numeric or alphanumeric prompts
			default:
			{
				def.defaultAnswer = deflt;
		    	break;
			}
    	}
    }
}

//=============================================================================
// METHOD    : SPELLcifHelper::getPythonResult()
//=============================================================================
PyObject* SPELLcifHelper::getPythonResult( const std::string& result, const SPELLpromptDefinition& def )
{
    PyObject* promptResult = Py_None;

	DEBUG("[CIF] Get prompt Python result from '" + result + "'");

	// Error cases
	if (result == PROMPT_CANCELLED || result == PROMPT_ERROR || result == PROMPT_TIMEOUT )
	{
		Py_RETURN_NONE;
	}

	// Headless procedure with prompt
	if (result == PROMPT_NOCLIENT)
	{
		return SSTRPY(result);
	}

    // Now translate the result to python type
    switch(def.typecode)
    {
    case LanguageConstants::PROMPT_OK:
    case LanguageConstants::PROMPT_CANCEL:
    case LanguageConstants::PROMPT_OK_CANCEL:
    case LanguageConstants::PROMPT_YES:
    case LanguageConstants::PROMPT_NO:
    case LanguageConstants::PROMPT_YES_NO:
    {
    	if (strToBoolean(result))
        {
            Py_RETURN_TRUE;
        }
        else
        {
            Py_RETURN_FALSE;
        }
        break;
    }
    default:
        // If LIST of options defined
        if ((def.options.size()>0))
        {
        	// LIST|NUM combination
        	if (def.typecode == (LanguageConstants::PROMPT_NUM | LanguageConstants::PROMPT_LIST))
        	{
            	if ((result.find(".") != std::string::npos) ||
            	   (result.find("e-") != std::string::npos) ||
             	   (result.find("e+") != std::string::npos))
             	{
                    promptResult = PyFloat_FromString( SSTRPY(result), NULL );
            	}
            	else
            	{
                    promptResult = PyLong_FromString( const_cast<char*>(result.c_str()), NULL, 0 );
            	}
        	}
        	// Other lists
        	else
        	{
        		promptResult = SSTRPY(result);
        	}
        }
        // If numeric prompt
        else if ((def.typecode & LanguageConstants::PROMPT_NUM)>0)
        {
        	// If it is a float number, convert to float
        	if ((result.find(".") != std::string::npos) ||
        	   (result.find("e-") != std::string::npos) ||
         	   (result.find("e+") != std::string::npos))
         	{
                promptResult = PyFloat_FromString( SSTRPY(result), NULL );
        	}
        	else
        	{
                promptResult = PyLong_FromString( const_cast<char*>(result.c_str()), NULL, 0 );
        	}
            if (promptResult == NULL)
            {
            	THROW_DRIVER_EXCEPTION("Error on prompt", "Unable to format answer to numeric value");
            }
        }
        // If date prompt
        else if ((def.typecode & LanguageConstants::PROMPT_DATE)>0)
        {
        	try
        	{
        		promptResult = SPELLpythonHelper::instance().pythonTime(result);
        	}
        	catch(SPELLcoreException& ex)
        	{
        		THROW_DRIVER_EXCEPTION("Error on prompt", "Unable to format answer to SPELL date");
        	}
        }
        else
        {
            promptResult = SSTRPY( result );
        }
        if (promptResult != NULL)
		{
        	Py_INCREF(promptResult);
		}
        else
        {
        	THROW_DRIVER_EXCEPTION("Error on prompt", "Unable to get answer");
        }
        break;
    }
    return promptResult;
}

//=============================================================================
// METHOD    : SPELLcifHelper::getResult()
//=============================================================================
std::string SPELLcifHelper::getResult( const std::string& result, const SPELLpromptDefinition& def )
{
	std::string promptResult = "";

    // Now translate the result to python type
    switch(def.typecode)
    {
    case LanguageConstants::PROMPT_OK:
    case LanguageConstants::PROMPT_CANCEL:
    case LanguageConstants::PROMPT_OK_CANCEL:
    case LanguageConstants::PROMPT_YES:
    case LanguageConstants::PROMPT_NO:
    case LanguageConstants::PROMPT_YES_NO:
    {
    	promptResult = def.expected[STRI(result)];
        break;
    }
    default:
        // If LIST of options defined
        if ((def.options.size()>0))
        {
        	int optionIndex = STRI(result);
        	// Conversion LIST|ALPHA
        	if ( (def.typecode & LanguageConstants::PROMPT_ALPHA)>0)
        	{
            	// If either way, the user has chosen to provide key:value pairs:
            	if (def.options[0].find(":") != std::string::npos)
            	{
            		// Search for the key that matches
                	std::vector<std::string>::const_iterator it;
                	for( it = def.options.begin(); it != def.options.end(); it++)
                	{
                		int idx = (*it).find(":");
                		std::string key = (*it).substr(0,idx);
                		SPELLutils::trim(key);
                		std::string value = (*it).substr(idx+1,(*it).size()-idx);
                		SPELLutils::trim(value);
                		if (result == key)
                		{
                			promptResult = value;
                		}
                	}
                	// If no key matches, take directly the corresponding option
                	if (promptResult == "")
                	{
                		promptResult = def.options[optionIndex];
                	}
            	}
            	// If no ':' symbol is found, take directly the corresponding option
            	else
            	{
            		promptResult = def.options[optionIndex];
            	}
        	}
        	// Conversion LIST|NUM
        	else if ( (def.typecode & LanguageConstants::PROMPT_NUM)>0)
        	{
        		promptResult = ISTR(STRI(result) + 1); // index correction
        	}
        	// LIST: return the key
        	else
        	{
            	// The result corresponds to the index of the key, not necessarily to the key value
            	// (numbering can be different)
				promptResult = def.expected[optionIndex];
        	}
        }
        else
        {
            promptResult = result;
        }
        break;
    }
    return promptResult;
}


//=============================================================================
// METHOD    : SPELLcifHelper::commandLinePrompt()
//=============================================================================
void SPELLcifHelper::displayPrompt( const SPELLpromptDefinition& def )
{
	std::cout << def.message << std::endl;
	if (def.options.size()>0)
	{
		unsigned int count = 0;
		std::string gap = "    ";
		std::cout << gap << "Options: " << std::endl;
		SPELLpromptDefinition::Options::const_iterator it;
		for( it = def.options.begin(); it != def.options.end(); it++)
		{
			std::string key = "";
			std::vector<std::string> tokens = SPELLutils::tokenize( (*it), ":" );
			key = tokens[0];
			SPELLutils::trim(key);
			std::cout << gap << "  " << (*it);
			if ( count < (def.options.size()-1) ) std::cout << ",\n";
			count++;
		} //for
		std::cout << std::endl;
	} //if
} //displayPrompt

//=============================================================================
// METHOD    : SPELLcifHelper::commandLinePrompt()
//=============================================================================
std::string SPELLcifHelper::commandLinePrompt( const SPELLpromptDefinition& def, bool useRaw )
{
    std::string answer = "";
    bool keepTrying = true;
    while(keepTrying)
    {
        answer= "";
        std::cout << std::endl;

        displayPrompt( def );

        std::cout << ">> "; // write cursor

        // Reset the stream otherwise the failbit may still be set from previous 'while' iterations
        std::cin.clear();

        // Get the answer from standard input
        std::getline(std::cin,answer);

        // Trim and check for errors (CTRL+C, CTRL+D, etc)
        SPELLutils::trim(answer);
        if ((std::cin.rdstate() & std::ifstream::failbit ) != 0 )
        {
        	return "";
        }

        // Repeat on empty answers
        if (answer== "") continue;

        // Check against options if defined
		if (def.options.size()>0)
		{
			SPELLpromptDefinition::Options::const_iterator eit;
			int count = 0;
			for( eit = def.expected.begin(); eit != def.expected.end(); eit++)
			{
				if (*eit == answer )
				{
					if (useRaw)
					{
						answer = ISTR(count);
					}
					keepTrying = false;
					break;
				}
				count++;
			}
			if (keepTrying)
			{
				std::cerr << "ERROR: Expected one of the following: " << std::endl;
				count = 0;
				for( eit = def.expected.begin(); eit != def.expected.end(); eit++)
				{
					std::cout << "   " << (*eit);
					if (count<def.expected.size()-1) std::cout << ", ";
					count++;
				}
				std::cout << std::endl;
			}
		}
		else
		{
			keepTrying = false;
		}
    } //while keep trying
    return answer;
}


//=============================================================================
// METHOD    : SPELLcifHelper::getRawAnswer()
//=============================================================================
std::string SPELLcifHelper::getRawAnswer( std::string answer, const SPELLpromptDefinition& def )
{
	std::string rawAnswer = "";
	bool answerNotFound = true;

	SPELLutils::trim(answer);

	if (answer != "")
	{
		if (def.options.size()>0)
		{
			// When there are a list of expexted values: boolean values or PROMPT_LIST.
			SPELLpromptDefinition::Options::const_iterator eit;
			int count = 0;
			for( eit = def.expected.begin(); eit != def.expected.end(); eit++)
			{
				if (*eit == answer )
				{
					rawAnswer = ISTR(count);  //index of the introduced value.
					answerNotFound = false;
					break;
				}
				count++;
			} //for

			if (answerNotFound)  //when answer does not match one of the expected options.
			{
				std::cerr << "ERROR: Expected one of the following: " << std::endl;
				count = 0;
				for( eit = def.expected.begin(); eit != def.expected.end(); eit++)
				{
					std::cout << "   " << (*eit);
					if (count<def.expected.size()-1) std::cout << ", ";
					count++;
				}
				std::cout << std::endl;
			} // if answer not Found

		} //if def options
		else {
			// There is no options. So free text or number expected.
			// Values are PROMPT_NUM, PROMPT_ALPHA or PROMPT_DATE when there are not PROMPT_LIST
			rawAnswer = answer;
		}
	} //if ! empty answer

	return rawAnswer;
} //getRawAnswer
