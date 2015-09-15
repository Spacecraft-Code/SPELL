// ################################################################################
// FILE       : SPELLutils.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the utilities
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
// Local includes ----------------------------------------------------------
#include "SPELL_UTIL/SPELLbase.H"
#include "SPELL_UTIL/SPELLerror.H"
#include "SPELL_UTIL/SPELLpythonMonitors.H"
// Project includes --------------------------------------------------------
#include "SPELL_WRP/SPELLpyHandle.H"
// System includes ---------------------------------------------------------
#include <time.h>

static int s_timeFormat = TIME_FORMAT_DEFAULT;



//============================================================================
// FUNCTION : PYSIZE
//============================================================================
int PYSIZE( PyObject* object )
{
	if (object == NULL) return -1;
    if (PyDict_Check(object))
    {
    	return PyDict_Size(object);
    }
    else if (PyList_Check(object))
    {
    	return PyList_Size(object);
    }
    else if (PyString_Check(object))
    {
    	return PyString_Size(object);
    }
    return -1;
};

//============================================================================
// FUNCTION : SPELLutils::hexstr
//============================================================================
std::string SPELLutils::hexstr( unsigned long i )
{
    std::ostringstream res;
    res << std::hex << i;
    return res.str();
};

//============================================================================
// FUNCTION : SPELLutils::octstr
//============================================================================
std::string SPELLutils::octstr( unsigned long i )
{
    std::ostringstream res;
    res << std::oct << i;
    return res.str();
};

//============================================================================
// FUNCTION : SPELLutils::binstr
//============================================================================
std::string SPELLutils::binstr( unsigned long i )
{
    std::string res;
    while(i)
    {
    	res.push_back((i&1)+'0');
    	i >>= 1;
    }
    if (res.empty())
    {
    	res = "0";
    }
    else
    {
    	std::reverse(res.begin(),res.end());
    }
    return res;
};

//============================================================================
// FUNCTION : SPELLutils::itostr
//============================================================================
std::string SPELLutils::itostr( long i )
{
    char buffer[50];
    bzero(buffer,50);
    sprintf( buffer, "%ld", i );
    return buffer;
};

//============================================================================
// FUNCTION : SPELLutils::strtoi
//============================================================================
long SPELLutils::strtoi( const std::string& s )
{
	if (s == "") return -1;
    return atoi(s.c_str());
};

//============================================================================
// FUNCTION : SPELLutils::pyObjectString
//============================================================================
std::string SPELLutils::pyObjectString( PyObject* obj )
{
	// PyString_AsString( PyObject_Str(x) )
	std::string result = "<?>";
	SPELLpyHandle str = PyObject_Str(obj);
	if (str.get() != NULL)
	{
		result = std::string(PyString_AsString(str.get()));
	}
	return result;
}

//============================================================================
// FUNCTION : SPELLutils::pyObjectRepr
//============================================================================
std::string SPELLutils::pyObjectRepr( PyObject* obj )
{
	// PyString_AsString( PyObject_Repr(x) )
	std::string result = "<?>";
	SPELLpyHandle str = PyObject_Repr(obj);
	if (str.get() != NULL)
	{
		result = PyString_AsString(str.get());
	}
	return result;
}

//============================================================================
// FUNCTION : SPELLutils::tokenize
//============================================================================
std::vector<std::string> SPELLutils::tokenize( const std::string& str, const std::string& delimiters )
{
    std::vector<std::string> tokens;

    // Skip delimiters at the beginning
    std::string::size_type lastPos = str.find_first_not_of(delimiters,0);

    // Find first non-delimiter
    std::string::size_type pos = str.find_first_of(delimiters, lastPos);

    while( std::string::npos != pos || std::string::npos != lastPos )
    {
        // Found a token, add it to the vector
        tokens.push_back( str.substr( lastPos, pos - lastPos ));
        // Skip delimiters
        lastPos = str.find_first_not_of(delimiters, pos);
        // Find next non-delimiter
        pos = str.find_first_of(delimiters, lastPos);
    }
    return tokens;
}

//============================================================================
// FUNCTION : SPELLutils::tokenizeData
//============================================================================
std::map<std::string, std::vector<BYTE> > SPELLutils::tokenizeData( const std::string& data )
{
    std::map<std::string, std::vector<BYTE> > tokens;
    std::string key;
    std::vector<BYTE> value;
    unsigned int keyLength;
    unsigned int valueLength;
    unsigned int byte;
    unsigned int pos;

    for( pos = 0 ; pos < data.size() ; )
    {
    	keyLength = ((unsigned char) data[pos]) * 256 + ((unsigned char) data[pos + 1]);
        pos += 2;
        key.assign(data.begin() + pos, data.begin() + pos + keyLength);
        pos += keyLength;

        valueLength  = (((unsigned char) data[pos]  ) << 24);
        valueLength += (((unsigned char) data[pos+1]) << 16);
        valueLength += (((unsigned char) data[pos+2]) << 8);
        valueLength += (unsigned char) data[pos+3];
        pos += 4;
        value.assign(data.begin() + pos, data.begin() + pos + valueLength);
        pos += valueLength;

        tokens[key] = value;
    } 

    return tokens;
}

//============================================================================
// FUNCTION : SPELLutils::tokenized
//============================================================================
std::vector<std::string> SPELLutils::tokenized( const std::string& str, const std::string& delimiters )
{
    std::vector<std::string> tokens;
    std::string::size_type delimPos = 0, tokenPos = 0, pos = 0;

    if(str.length()<1) return tokens;
    while(1)
    {
        delimPos = str.find_first_of(delimiters, pos);
        tokenPos = str.find_first_not_of(delimiters, pos);

        if(std::string::npos != delimPos)
        {
            if(std::string::npos != tokenPos)
            {
                if(tokenPos<delimPos)
                {
                    tokens.push_back(str.substr(pos,delimPos-pos));
                }
                else
                {
                    tokens.push_back("");
                }
            }
            else
            {
                tokens.push_back("");
            }
            pos = delimPos+1;
        }
        else
        {
            if(std::string::npos != tokenPos)
            {
                tokens.push_back(str.substr(pos));
            }
            else
            {
                tokens.push_back("");
            }
            break;
        }
    }
    return tokens;
}

//============================================================================
// FUNCTION : SPELLutils::trim
//============================================================================
void SPELLutils::trim( std::string& str )
{
    std::string::size_type pos = str.find_last_not_of(' ');
    if(pos != std::string::npos)
    {
        str.erase(pos + 1);
        pos = str.find_first_not_of(' ');
        if(pos != std::string::npos) str.erase(0, pos);
    }
    else
    {
        str.erase(str.begin(), str.end());
    }
}

//============================================================================
// FUNCTION : SPELLutils::trim
//============================================================================
void SPELLutils::trim( std::string& str, std::string characters )
{
    std::string::size_type pos;
    while(true)
    {
        pos = str.find_first_of(characters);
        if (pos != std::string::npos)
        {
            str.erase(pos);
        }
        else
        {
            break;
        }
    }
}

//============================================================================
// FUNCTION : SPELLutils::toLower
//============================================================================
std::string SPELLutils::toLower( const std::string& str )
{
	std::locale loc;
	std::string resp = "";
	for (size_t i=0; i<str.length(); ++i)
	{
	    resp += tolower(str[i],loc);
	}
	return resp;
}

//============================================================================
// FUNCTION : SPELLutils::toUpper
//============================================================================
std::string SPELLutils::toUpper( const std::string& str )
{
	std::locale loc;
	std::string resp = "";
	for (size_t i=0; i<str.length(); ++i)
	{
	    resp += toupper(str[i],loc);
	}
	return resp;
}

//============================================================================
// FUNCTION : SPELLutils::replace
//============================================================================
void SPELLutils::replace( std::string& str, std::string original, std::string newstr )
{
    std::string::size_type pos;
    while(true)
    {
        pos = str.find(original);
        if (pos != std::string::npos)
        {
            str.replace(pos, original.size(), newstr.c_str());
        }
        else
        {
            break;
        }
    }
}

//============================================================================
// FUNCTION : SPELLutils::resolvePath
//============================================================================
std::string SPELLutils::resolvePath( const std::string& path )
{
    std::string resolved = path;
    std::vector<std::string> tokens = tokenize( resolved, PATH_SEPARATOR );
    std::vector<std::string>::iterator it;
    for ( it = tokens.begin(); it != tokens.end(); it++)
    {
        if ( (*it).find_first_of("$") != std::string::npos)
        {
            std::string varname = (*it).substr(1, (*it).size()-1);
            char* value = getenv( varname.c_str() );
            if (value == NULL)
			{
            	THROW_EXCEPTION("Cannot resolve path", "Cannot find variable " + (*it), SPELL_ERROR_ENVIRONMENT );
			}
            (*it) = value;
        }
    }
    resolved = "";
    for ( it = tokens.begin(); it != tokens.end(); it++)
    {
        if (resolved.size()>0) resolved += "/";
        resolved += (*it);
    }
    return resolved;
}

//============================================================================
// FUNCTION:    SPELLutils::basePath
//============================================================================
std::string SPELLutils::basePath( const std::string& path )
{
    std::size_t pos = path.find_last_of( PATH_SEPARATOR );
    if ( pos != std::string::npos )
    {
        return path.substr(0, pos);
    }
    else
    {
        return path;
    }
}

//============================================================================
// FUNCTION : SPELLutils::pathExists
//============================================================================
bool SPELLutils::pathExists( const std::string& path )
{
    struct stat st;
    return (stat( path.c_str(),&st) == 0);
}

//============================================================================
// FUNCTION : SPELLutils::isDirectory
//============================================================================
bool SPELLutils::isDirectory( const std::string& path )
{
    struct stat buf;
    bool isDir = false;
    if (stat(path.c_str(),&buf) == 0)
    {
        isDir = S_ISDIR( buf.st_mode );
    }
    return isDir;
}

//============================================================================
// FUNCTION : SPELLutils::isFile
//============================================================================
bool SPELLutils::isFile( const std::string& path )
{
    struct stat buf;
    bool isFile = false;
    if (stat(path.c_str(),&buf) == 0)
    {
        isFile = S_ISREG( buf.st_mode );
    }
    return isFile;
}

//============================================================================
// FUNCTION : SPELLutils::fileCopy
//============================================================================
void SPELLutils::copyFile( const std::string& sourcePath, const std::string& targetPath )
{
	if (!pathExists(sourcePath))
	{
		THROW_EXCEPTION("Cannot copy file", "File not found: " + sourcePath, SPELL_ERROR_FILESYSTEM);
	}
	if (!pathExists( basePath( targetPath )))
	{
		THROW_EXCEPTION("Cannot copy file", "Target directory not found: " + basePath(targetPath), SPELL_ERROR_FILESYSTEM);
	}
	std::ifstream infile( sourcePath.c_str() , std::ios_base::binary);
	std::ofstream outfile( targetPath.c_str(), std::ios_base::binary);
	outfile << infile.rdbuf();
	outfile.flush();
	outfile.close();
}

//============================================================================
// FUNCTION : SPELLutils::getFilesInDir
//============================================================================
std::list<std::string> SPELLutils::getFilesInDir( const std::string& path )
{
    DIR *dirp;
    struct dirent *dp;

    std::list<std::string> files;

    dirp = opendir( path.c_str() );
    if (dirp)
    {
		while ( (dp = readdir(dirp)) != NULL )
		{
			if (dp->d_type & DT_REG)
			{
				files.push_back(dp->d_name);
			}
			// IMPORTANT: in some filesystems, the type may be impossible
			// to get with readdir. In these cases we need to use stat().
			else if (dp->d_type == DT_UNKNOWN)
			{
				if (isFile( path + PATH_SEPARATOR + std::string(dp->d_name)))
				{
					files.push_back(dp->d_name);
				}
			}
			// Ignore other types like symbolic links, sockets, etc.
		}
		closedir(dirp);
    }
    else
    {
    	LOG_ERROR("Failed to open directory '" + path + "' to read");
    }
    return files;
}

//============================================================================
// FUNCTION : SPELLutils::getSubdirs
//============================================================================
std::list<std::string> SPELLutils::getSubdirs( const std::string& path )
{
    DIR* dirp;
    struct dirent *dp;

    std::string dot = ".";
    std::string pdot = "..";
    std::string svn = ".svn";
    std::list<std::string> subdirs;

    dirp = opendir( path.c_str() );
    if (dirp)
    {
		while ( (dp = readdir(dirp)) != NULL )
		{
			if (dp->d_type & DT_DIR)
			{
				if ( dp->d_name != dot && dp->d_name != pdot && dp->d_name != svn )
				{
					subdirs.push_back(dp->d_name);
				}
			}
			// IMPORTANT: in some filesystems, the type may be impossible
			// to get with readdir. In these cases we need to use stat().
			else if (dp->d_type == DT_UNKNOWN)
			{
				if ( isDirectory( path + PATH_SEPARATOR + std::string(dp->d_name) ) )
				{
					if ( dp->d_name != dot && dp->d_name != pdot && dp->d_name != svn )
					{
						subdirs.push_back(dp->d_name);
					}
				}
			}
			// Ignore other types like symbolic links, sockets, etc.
		}
		closedir(dirp);
    }
    else
    {
    	LOG_ERROR("Failed to open directory '" + path + "' to get subdirs");
    }
    return subdirs;
}

//============================================================================
// FUNCTION:    SPELLutils::setTimeFormat()
//============================================================================
void SPELLutils::setTimeFormat( int format )
{
	s_timeFormat = format;
}

//============================================================================
// FUNCTION:    SPELLutils::timestamp
//============================================================================
std::string SPELLutils::timestamp()
{
	SPELLdateDesc date = getSystemDate();

    std::string year  = ISTR(date.year);
    std::string month = ISTR(date.month);
    if (month.size()==1) month = "0" + month;
    std::string day   = ISTR(date.day);
    if (day.size()==1) day = "0" + day;
    std::string hours = ISTR(date.hours);
    if (hours.size()==1) hours = "0" + hours;
    std::string mins = ISTR(date.minutes);
    if (mins.size()==1) mins = "0" + mins;
    std::string secs = ISTR(date.seconds);
    if (secs.size()==1) secs = "0" + secs;
    std::string ddd  = ISTR(date.yday);
    if (ddd.size()==1) ddd = "00" + ddd;
    else if (ddd.size()==2) ddd = "0" + ddd;

    std::string output = "";

    switch(s_timeFormat)
    {
    case TIME_FORMAT_SLASH:
    	// Example: 2009/09/01 12:49:54
    	output = year + "/" + month + "/" + day + " " + hours + ":" + mins + ":" + secs;
    	break;
    case TIME_FORMAT_DOT:
    	// Example: 2009.130.12.49.54
    	output = year + "." + ddd + "." + hours + "." + mins + "." + secs;
    	break;
    case TIME_FORMAT_DEFAULT:
    default:
    	// Example: 2009-09-01 12:49:54
    	output = year + "-" + month + "-" + day + " " + hours + ":" + mins + ":" + secs;
    	break;
    }

    return output;
}

//=============================================================================
// METHOD: SPELLutils::timestampUsec
//=============================================================================
std::string SPELLutils::timestampUsec()
{
	const std::string complete("000000");
	SPELLtimeDesc time = getSystemTime();
    std::string sec  = ISTR(time.seconds);
    std::string usec = ISTR(time.useconds);
    if (usec.length()<6)
    {
    	usec += complete.substr(0,6-usec.length());
    }
    return (sec + usec);
}

//============================================================================
// FUNCTION:    SPELLutils::deleteFile
//============================================================================
void SPELLutils::deleteFile( const std::string& file )
{
	if (pathExists(file) && isFile(file))
	{
		::remove( file.c_str() );
	}
}

//============================================================================
// FUNCTION:    SPELLutils::fileTimestamp
//============================================================================
std::string SPELLutils::fileTimestamp()
{
	SPELLdateDesc date = getSystemDate();

    std::string year  = ISTR(date.year);
    std::string month = ISTR(date.month);
    if (month.size()==1) month = "0" + month;
    std::string day   = ISTR(date.day);
    if (day.size()==1) day = "0" + day;
    std::string hours = ISTR(date.hours);
    if (hours.size()==1) hours = "0" + hours;
    std::string mins = ISTR(date.minutes);
    if (mins.size()==1) mins = "0" + mins;
    std::string secs = ISTR(date.seconds);
    if (secs.size()==1) secs = "0" + secs;

    // 2009-09-01_124954
    return year + "-" + month + "-" + day + "_" + hours + mins + secs;
}

//============================================================================
// FUNCTION:    SPELLutils::envVar
//============================================================================
void envVar( const std::string& name )
{
    char* value = getenv(name.c_str());
    if (value == NULL)
    {
        std::cerr << "* " << std::setw(15) << name << ": (NOT DEFINED)" << std::endl;
    }
    else
    {
        std::cerr << "* " << std::setw(15) << name << ": " << value << std::endl;
    }
}

//============================================================================
// FUNCTION:    SPELLutils::showEnvironment
//============================================================================
void SPELLutils::showEnvironment()
{
    std::cerr << "==============================================================================" << std::endl;
    std::cerr << "     SPELL environment configuration" << std::endl;
    std::cerr << "==============================================================================" << std::endl;
    envVar("SPELL_HOME");
    envVar("SPELL_COTS");
    envVar("SPELL_DATA");
    envVar("SPELL_CONFIG");
    envVar("SPELL_SYS_DATA");
    envVar("SPELL_LOG");
    envVar("SPELL_PROCS");
    std::cerr << "==============================================================================" << std::endl;
}

//============================================================================
// FUNCTION:    SPELLutils::getSPELL_HOME()
//============================================================================
std::string SPELLutils::getSPELL_HOME()
{
	char* value = getenv("SPELL_HOME");
	if (value == NULL)
	{
	    THROW_EXCEPTION("Unable to obtain SPELL_HOME", "Variable not defined in environment", SPELL_ERROR_ENVIRONMENT);
	}
	return value;
}

//============================================================================
// FUNCTION:    SPELLutils::getSPELL_DATA()
//============================================================================
std::string SPELLutils::getSPELL_DATA()
{
	char* value = getenv("SPELL_DATA");
	if (value == NULL)
	{
	    return getSPELL_HOME() + PATH_SEPARATOR + "data";
	}
	return value;
}

//============================================================================
// FUNCTION:    SPELLutils::getSPELL_LOG()
//============================================================================
std::string SPELLutils::getSPELL_LOG()
{
	char* value = getenv("SPELL_LOG");
	if (value == NULL)
	{
	    return getSPELL_HOME() + PATH_SEPARATOR + "log";
	}
	return value;
}

//============================================================================
// FUNCTION:    SPELLutils::getSPELL_CONFIG()
//====================================================nnn========================
std::string SPELLutils::getSPELL_CONFIG()
{
	char* value = getenv("SPELL_CONFIG");
	if (value == NULL)
	{
	    return getSPELL_HOME() + PATH_SEPARATOR + "config";
	}
	return value;
}

//============================================================================
// FUNCTION:    SPELLutils::dumpInterpreterInfo()
//============================================================================
void SPELLutils::dumpInterpreterInfo( const std::string& id )
{
	LOG_INFO("Begin dump");
	// Cleanup previous files
	std::string dataDir = getSPELL_DATA() + PATH_SEPARATOR + "Runtime" + PATH_SEPARATOR;
	std::list<std::string> files = getFilesInDir(dataDir);
	std::list<std::string>::iterator it;
	for( it = files.begin(); it != files.end(); it++)
	{
		std::string fileName = (*it);
		if (fileName.find( id + "_interpreter_state") != std::string::npos )
		{
			deleteFile( dataDir + fileName );
		}
		if (fileName.find( id + "_thread_state") != std::string::npos )
		{
			deleteFile( dataDir + fileName );
		}
		if (fileName.find( id + "_frame_state") != std::string::npos )
		{
			deleteFile( dataDir + fileName );
		}
	}

	LOG_INFO("Get interpreter head");
	PyInterpreterState* currIS = PyInterpreterState_Head();

	// Count the interpreter state
	int numIS = 0;
	// For each interpreter state
	while(currIS != NULL)
	{
		LOG_INFO("Dump information for interpreter state " + ISTR(numIS));
		std::string filename = dataDir + id + "_interpreter_state_" + ISTR(numIS) + ".dump";
		std::ofstream dumpFile;
		dumpFile.open( filename.c_str() , std::ios::out );

		// Main data
		dumpFile << "INTERPRETER STATE " << numIS << " DATA" << std::endl;
		dumpFile << "--------------------------------------" << std::endl;
		dumpFile << "Address     : " << PSTR(currIS) << std::endl;
		dumpFile << "Next address: " << PSTR(currIS->next) << std::endl;
		dumpFile << "Modules     : " << PYSIZE(currIS->modules)  << " items." << std::endl;
		dumpFile << "Sysdict     : " << PYSIZE(currIS->sysdict)  << " items." << std::endl;
		dumpFile << "Builtins    : " << PYSIZE(currIS->builtins) << " items." << std::endl;
		dumpFile << "Reloading   : " << PYSIZE(currIS->modules_reloading)  << " items." << std::endl;
		dumpFile << "Search path : " << PYSIZE(currIS->codec_search_path)  << " items." << std::endl;
		dumpFile << "Search cache: " << PYSIZE(currIS->codec_search_cache) << " items." << std::endl;
		dumpFile << "Error regst.: " << PYSIZE(currIS->codec_error_registry) << " items." << std::endl;
		dumpFile << "DL flags    : " << currIS->dlopenflags << std::endl;

		// Count thread states
		int numTS = 0;
		PyThreadState* currTS = currIS->tstate_head;
		while( currTS != NULL )
		{
			dumpThreadStateInfo( id, currTS, numIS, numTS );
			numTS++;
			currTS = currTS->next;
		}
		dumpFile << "Thr. states : " << numTS << std::endl;

		// Close the interpreter state dump, no more to add
		dumpFile.flush();
		dumpFile.close();

		// Next interpreter state
		currIS = currIS->next;
		numIS++;
	}
	LOG_INFO("Finish dump");
}

//============================================================================
// FUNCTION:    SPELLutils::dumpThreadStateInfo()
//============================================================================
void SPELLutils::dumpThreadStateInfo( const std::string& id, PyThreadState* ts, int iStateId, int TStateId  )
{
	LOG_INFO("Dump information for thread state " + ISTR(iStateId) + "." + ISTR(TStateId));
	std::string dataDir = getSPELL_DATA() + PATH_SEPARATOR + "Runtime" + PATH_SEPARATOR;
	std::string filename = dataDir + id + "_thread_state_" + ISTR(iStateId) + "." + ISTR(TStateId) + ".dump";
	std::ofstream dumpFile;
	dumpFile.open( filename.c_str() , std::ios::out );

	dumpFile << "THREAD STATE " << TStateId << " DATA" << std::endl;
	dumpFile << "--------------------------------------" << std::endl;
	dumpFile << "Address           : " << PSTR(ts) << std::endl;
	dumpFile << "Next address      : " << PSTR(ts->next) << std::endl;
	dumpFile << "IState address    : " << PSTR(ts->interp) << std::endl;
	dumpFile << "Recursion depth   : " << ts->recursion_depth << std::endl;
	dumpFile << "Tracing flag      : " << ts->tracing << std::endl;
	dumpFile << "Use tracing flg   : " << ts->use_tracing << std::endl;
	dumpFile << "Profile function  : " << ts->c_profilefunc << std::endl;
	dumpFile << "Trace function    : " << ts->c_tracefunc << std::endl;
	dumpFile << "Profile object    : " << PYREPR(ts->c_profileobj) << std::endl;
	dumpFile << "Trace object      : " << PYREPR(ts->c_traceobj) << std::endl;
	dumpFile << "Current exc. type : " << PYREPR(ts->curexc_type) << std::endl;
	dumpFile << "Current exc. value: " << PYREPR(ts->curexc_value) << std::endl;
	dumpFile << "Current exc. tback: " << PYREPR(ts->curexc_traceback) << std::endl;
	dumpFile << "Exception type    : " << PYREPR(ts->exc_type) << std::endl;
	dumpFile << "Exception value   : " << PYREPR(ts->exc_value) << std::endl;
	dumpFile << "Exception tback   : " << PYREPR(ts->exc_traceback) << std::endl;
	dumpFile << "Main dict         : " << PYSIZE(ts->dict) << " items." << std::endl;
	dumpFile << "Tick counter      : " << ts->tick_counter << std::endl;
	dumpFile << "GIL state counter : " << ts->gilstate_counter << std::endl;
	dumpFile << "Async. exception  : " << PYREPR(ts->async_exc) << std::endl;
	dumpFile << "Thread id         : " << ts->thread_id << std::endl;

	// Count frames
	int numFR = 0;
	PyFrameObject* currFR = ts->frame;
	while( currFR != NULL )
	{
		dumpFrameInfo( id, currFR, iStateId, TStateId, numFR );
		numFR++;
		currFR = currFR->f_back;
	}
	dumpFile << "Number of frames  : " << numFR << std::endl;

	// Close the thread state dump, no more to add
	dumpFile.flush();
	dumpFile.close();
}

//============================================================================
// FUNCTION:    SPELLutils::dumpFrameInfo()
//============================================================================
void SPELLutils::dumpFrameInfo( const std::string& id, PyFrameObject* frame, int iStateId, int TStateId, int FrameId )
{
	LOG_INFO("Dump information for frame " + ISTR(iStateId) + "." + ISTR(TStateId) + "." + ISTR(FrameId));

	std::string dataDir = getSPELL_DATA() + PATH_SEPARATOR + "Runtime" + PATH_SEPARATOR;
	std::string filename = dataDir + id + "_frame_state_" + ISTR(iStateId) + "." + ISTR(TStateId) + "." + ISTR(FrameId) + ".dump";
	std::ofstream dumpFile;
	dumpFile.open( filename.c_str() , std::ios::out );

	dumpFile << "FRAME STATE " << TStateId << " DATA" << std::endl;
	dumpFile << "--------------------------------------" << std::endl;
	dumpFile << "Address              : " << PSTR(frame) << std::endl;
	dumpFile << "Next address         : " << PSTR(frame->f_back) << std::endl;
	dumpFile << "Thread state address : " << PSTR(frame->f_tstate) << std::endl;
	dumpFile << "Last instruction     : " << frame->f_lasti << std::endl;
	dumpFile << "Last line            : " << frame->f_lineno << std::endl;
	dumpFile << "Try blocks count     : " << frame->f_iblock << std::endl;
	dumpFile << "Try blocks           : " << PSTR(frame->f_blockstack) << std::endl;
	dumpFile << "Value stack          : " << PSTR(frame->f_valuestack) << std::endl;
	dumpFile << "Stack top            : " << PSTR(frame->f_stacktop) << std::endl;
	dumpFile << "Stack count          : " << (frame->f_stacktop - frame->f_valuestack) << std::endl;
	dumpFile << "Fast locals          : " << (frame->f_code->co_nlocals-1) << std::endl;
	dumpFile << "Exception type       : " << PYREPR(frame->f_exc_type) << std::endl;
	dumpFile << "Exception value      : " << PYREPR(frame->f_exc_value) << std::endl;
	dumpFile << "Exception traceback  : " << PYREPR(frame->f_exc_traceback) << std::endl;
	dumpFile << "Trace function       : " << PYREPR(frame->f_trace) << std::endl;
	dumpFile << "Builtins             : " << PYSIZE(frame->f_builtins) << std::endl;
	dumpFile << "Globals              : " << PYSIZE(frame->f_globals) << std::endl;
	dumpFile << "Locals               : " << PYSIZE(frame->f_locals) << std::endl;
	dumpFile << "Code                 : " << PYCREPR(frame->f_code) << std::endl;

	// Close the frame state dump, no more to add
	dumpFile.flush();
	dumpFile.close();
}

//============================================================================
// FUNCTION:    SPELLutils::dumpString()
//============================================================================
std::string SPELLutils::dumpString( const std::string& str )
{
    std::ostringstream res;
    std::string ascii;
    bool first = true;

    for(unsigned int i = 0 ; i < str.size() ; i++)
    {
        if (i % 16 == 0)
        {
            if(!ascii.empty())
            {
                res << " " << ascii;
                ascii = "";
            }
            res << std::endl << std::hex << std::setw(4) << std::setfill('0') << i << " ";
            first = true;
        }
        if(!first)
            res << " ";

        first = false;
        res << std::hex << std::setw(2) << std::setfill('0') << (unsigned int) ((unsigned char) str[i]);
        ascii += (str[i] > 31 && str[i] < 128 ? str[i] : '.');
    }

    if(!ascii.empty())
    {
        while(ascii.size() < 16)
        {
            res << "   ";
            ascii += " ";
        }
        res << " " << ascii;
    }

    return res.str();
}

//============================================================================
// FUNCTION:    SPELLutils::fileSize()
//============================================================================
unsigned long SPELLutils::fileSize( const std::string& fullpath )
{
    std::ifstream input(fullpath.c_str(), std::ios::in | std::ios::binary | std::ios::ate);
    return input.tellg();
}

//============================================================================
// FUNCTION:    SPELLutils::resolve()
//============================================================================
std::string SPELLutils::resolve( const std::string& hostname )
{
    struct hostent* entry;

    entry = gethostbyname(hostname.c_str());

    if(entry == NULL)
        return "";

    std::string result = inet_ntoa(*((struct in_addr*) entry->h_addr_list[0]));
    return result;
}

//============================================================================
// FUNCTION:    SPELLutils::getSystemTime()
//============================================================================
SPELLutils::SPELLtimeDesc SPELLutils::getSystemTime()
{
	SPELLtimeDesc timed;
	struct timespec abstime;
	clock_gettime(0, &abstime);
    timed.seconds = abstime.tv_sec;
    timed.useconds = abstime.tv_nsec/1000;
    return timed;
}

//============================================================================
// FUNCTION:    SPELLutils::getSystemDate()
//============================================================================
SPELLutils::SPELLdateDesc SPELLutils::getSystemDate()
{
	SPELLdateDesc date;
    time_t t;
    ::time(&t);
    struct tm* theTime;

    theTime = ::gmtime( &t );

    date.year = 1900 + theTime->tm_year;
    date.month = theTime->tm_mon+1;
    date.day = theTime->tm_mday;
    date.hours = theTime->tm_hour;
    date.minutes = theTime->tm_min;
    date.seconds = theTime->tm_sec;
    date.yday = theTime->tm_yday + 1;

    return date;
}
