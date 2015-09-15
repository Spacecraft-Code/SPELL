// ################################################################################
// FILE       : SPELLipcMessage.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the IPC message
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
#include "SPELL_IPC/SPELLipcMessage.H"
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLutils.H"

// DEFINES /////////////////////////////////////////////////////////////////
#define DEFAULT_COMPRESSED_FLAG false

//=============================================================================
// FUNCTION: StringToMessageType
//=============================================================================
inline SPELLipcMessageType StringToMessageType( std::string str )
{
    for(unsigned int idx=0; idx<MessageType::NumTypes; idx++)
    {
        if (MessageType::TypeStr[idx] == str) return (SPELLipcMessageType) idx;
    }
    return MSG_TYPE_UNKNOWN;
}

//=============================================================================
// CONSTRUCTOR: SPELLipcMessage::SPELLipcMessage
//=============================================================================
SPELLipcMessage::SPELLipcMessage()
{
    m_id = "<void>";
    m_sequence = -1;
    m_type = MSG_TYPE_NOTYPE;
    m_senderId = MessageId::GENERIC_ID;
    m_receiverId = MessageId::GENERIC_ID;
    m_compressed = DEFAULT_COMPRESSED_FLAG;
    m_key = -1;
}

//=============================================================================
// CONSTRUCTOR: SPELLipcMessage::SPELLipcMessage
//=============================================================================
SPELLipcMessage::SPELLipcMessage( std::string id )
{
    m_id = id;
    m_sequence = -1;
    m_type = MSG_TYPE_NOTYPE;
    m_senderId = MessageId::GENERIC_ID;
    m_receiverId = MessageId::GENERIC_ID;
    m_compressed = DEFAULT_COMPRESSED_FLAG;
    m_key = -1;
}

//=============================================================================
// CONSTRUCTOR: SPELLipcMessage::SPELLipcMessage
//=============================================================================
SPELLipcMessage::SPELLipcMessage( const SPELLipcMessage& msg )
{
	m_id = msg.m_id;
	m_sequence = msg.m_sequence;
	m_type = msg.m_type;
	m_senderId = msg.m_senderId;
	m_receiverId = msg.m_receiverId;
	m_key = msg.m_key;
	m_compressed = msg.m_compressed;
	m_properties = msg.m_properties;
}

//=============================================================================
// CONSTRUCTOR: SPELLipcMessage::SPELLipcMessage
//=============================================================================
SPELLipcMessage::SPELLipcMessage( SPELLipcMessage* msg )
{
	assert(msg != NULL);
	m_id = msg->m_id;
	m_sequence = msg->m_sequence;
	m_type = msg->m_type;
	m_senderId = msg->m_senderId;
	m_receiverId = msg->m_receiverId;
	m_key = msg->m_key;
	m_compressed = msg->m_compressed;
	m_properties = msg->m_properties;
}

//=============================================================================
// CONSTRUCTOR: SPELLipcMessage:SPELLipcMessage
//=============================================================================
SPELLipcMessage::SPELLipcMessage( std::string id, Properties properties )
{
    m_id = id;
    m_sequence = -1;
    m_type = MSG_TYPE_NOTYPE;
    m_senderId = MessageId::GENERIC_ID;
    m_receiverId = MessageId::GENERIC_ID;
    m_compressed = DEFAULT_COMPRESSED_FLAG;
    m_key = -1;
    Properties::iterator it;
    for( it = properties.begin(); it != properties.end(); it++ )
    {
        m_properties.insert( std::make_pair( it->first, it->second ) );
        if (it->first == MessageField::FIELD_SEQUENCE)
        {
        	m_sequence = STRI(it->second);
        }
    }
}

//=============================================================================
// OPERATOR: assignment
//=============================================================================
SPELLipcMessage& SPELLipcMessage::operator=( const SPELLipcMessage& copy )
{
	if (this == &copy)
	{
		return *this;
	}
	m_id = copy.m_id;
	m_sequence = copy.m_sequence;
	m_type = copy.m_type;
	m_senderId = copy.m_senderId;
	m_receiverId = copy.m_receiverId;
	m_key = copy.m_key;
	m_compressed = copy.m_compressed;
	m_properties = copy.m_properties;
	return *this;
}

//=============================================================================
// DESTRUCTOR: SPELLipcMessage:SPELLipcMessage
//=============================================================================
SPELLipcMessage::~SPELLipcMessage()
{
    m_properties.clear();
}

//=============================================================================
// METHOD: SPELLipcMessage:set
//=============================================================================
void SPELLipcMessage::set( std::string key, std::string value )
{
    m_properties[key] = value;
}

//=============================================================================
// METHOD: SPELLipcMessage:set
//=============================================================================
void SPELLipcMessage::set( std::string key, const std::vector<BYTE>& vec )
{
    std::string v;
    v.assign(vec.begin(), vec.end());
    m_properties[key] = v;
}

//=============================================================================
// METHOD: SPELLipcMessage:set
//=============================================================================
void SPELLipcMessage::set( std::string key, const std::string::iterator& startPos, const std::string::iterator& endPos)
{
    m_properties[key].assign(startPos, endPos);
}

//=============================================================================
// METHOD: SPELLipcMessage:get
//=============================================================================
std::string SPELLipcMessage::get( std::string key ) const
{
    Properties::const_iterator it = m_properties.find(key);
    if (it == m_properties.end())
    {
        return "";
    }
    return it->second;
}

//=============================================================================
// METHOD: SPELLipcMessage:getRef
//=============================================================================
std::string& SPELLipcMessage::getRef( std::string key )
{
    static std::string empty;

    Properties::iterator it = m_properties.find(key);
    if (it == m_properties.end())
    {
        return empty;
    }

    return it->second;
}

//=============================================================================
// METHOD: SPELLipcMessage:hasField
//=============================================================================
bool SPELLipcMessage::hasField( std::string field ) const
{
    Properties::const_iterator it = m_properties.find(field);
    return (it != m_properties.end());
}

//=============================================================================
// METHOD: SPELLipcMessage:dataItem
//=============================================================================
std::string SPELLipcMessage::dataItem(const std::string& key, const std::string& value) const
{
    std::string item;
    unsigned int keyLength;
    unsigned int valueLength;

    keyLength = key.size();

    item += (char) ((keyLength & 0xFF00) >> 8);
    item += (char) (keyLength  & 0x00FF);
    item += key;
    
    valueLength = value.size();
    item += (char) ((valueLength & 0xFF000000) >> 24);
    item += (char) ((valueLength & 0x00FF0000) >> 16);
    item += (char) ((valueLength & 0x0000FF00) >> 8);
    item += (char) (valueLength  & 0x000000FF);

    item += value;

    //DEBUG("Item: " + SPELLutils::dumpString(item));
    
    return item;
}

//=============================================================================
// METHOD: SPELLipcMessage:setCompressed
//=============================================================================
void SPELLipcMessage::setCompressed( bool compressed )
{
	m_compressed = compressed;
}

//=============================================================================
// METHOD: SPELLipcMessage:getCompressed
//=============================================================================
bool SPELLipcMessage::getCompressed() const
{
	return m_compressed;
}

//=============================================================================
// METHOD: SPELLipcMessage:data
//=============================================================================
std::string SPELLipcMessage::data() const
{
    std::string data = "";
    Properties::const_iterator it;

    data += dataItem(MessageField::FIELD_SENDER_ID, getSender());
    data += dataItem(MessageField::FIELD_RECEIVER_ID, getReceiver());
    data += dataItem(MessageField::FIELD_SEQUENCE, ISTR(m_sequence));
    data += dataItem(MessageField::FIELD_ID, getId());
    data += dataItem(MessageField::FIELD_TYPE, MessageType::TypeStr[getType()]);
    data += dataItem(MessageField::FIELD_IPC_KEY, ISTR(getKey()));

    for( it = m_properties.begin(); it != m_properties.end(); it++ )
        data += dataItem(it->first, it->second);

    return data;
}

//=============================================================================
// METHOD: SPELLipcMessage:dataStr
//=============================================================================
std::string SPELLipcMessage::dataStr() const
{
	std::string result = "";
    Properties::const_iterator it;
    for( it = m_properties.begin(); it != m_properties.end(); it++ )
    {
    	result += it->first + "," + it->second + "|";
    }
    result += MessageField::FIELD_SENDER_ID + "," + getSender() + "|";
    result += MessageField::FIELD_RECEIVER_ID + "," + getReceiver() + "|";
    result += MessageField::FIELD_SEQUENCE + "," + ISTR(m_sequence) + "|" ;
    result += MessageField::FIELD_ID + "," + getId() + "|";
    result += MessageField::FIELD_TYPE + "," + MessageType::TypeStr[getType()] + "|";
    result += MessageField::FIELD_IPC_KEY + "," + ISTR(getKey());
    return result;
}

//=============================================================================
// METHOD: SPELLipcMessage:requestId
//=============================================================================
std::string SPELLipcMessage::requestId() const
{
    std::string senderId = getSender();
    std::string receiverId = getReceiver();
    std::string reqId = ISTR(getKey()) + "-" + receiverId + "-" + senderId;

    if (m_sequence != -1)
    {
    	reqId += ":" + ISTR(m_sequence);
    }
    return reqId;
}

//=============================================================================
// METHOD: SPELLipcMessage:responseId
//=============================================================================
std::string SPELLipcMessage::responseId() const
{
    std::string senderId = getSender();
    std::string receiverId = getReceiver();
    std::string reqId = ISTR(getKey()) + "-" + senderId + "-" + receiverId;

    if (m_sequence != -1)
    {
    	reqId += ":" + ISTR(m_sequence);
    }
    return reqId;
}

//=============================================================================
// METHOD: SPELLipcMessage:fromData
//=============================================================================
void SPELLipcMessage::fromData( std::string data )
{
    //DEBUG("     Building message from " << data);
    std::string key;
    std::string value;

    m_properties.clear();

    std::map<std::string, std::vector<BYTE> > pairs = SPELLutils::tokenizeData(data);
    std::map<std::string, std::vector<BYTE> >::iterator it;

    for( it = pairs.begin(); it != pairs.end(); it++)
    {
        key = it->first;
        value.assign(it->second.begin(), it->second.end());

        //DEBUG("Key: " + key + " Value: " + SPELLutils::dumpString(value));

        if (key == MessageField::FIELD_ID)
        {
            m_id = value;
            //DEBUG("     SPELLipcMessage ID: " << m_id);
        }
        else if (key == MessageField::FIELD_TYPE)
        {
            m_type = StringToMessageType(value);
            //DEBUG("     SPELLipcMessage Type: " << m_type);
        }
        else if (key == MessageField::FIELD_SENDER_ID)
        {
            m_senderId = value;
            //DEBUG("     SPELLipcMessage Sender: " << m_senderId);
        }
        else if (key == MessageField::FIELD_RECEIVER_ID)
        {
            m_receiverId = value;
            //DEBUG("     SPELLipcMessage Receiver: " << m_receiverId);
        }
        else if (key == MessageField::FIELD_SEQUENCE)
        {
            m_sequence = STRI(value);
            //DEBUG("     SPELLipcMessage Sequence: " << m_sequence);
        }
        else if (key == MessageField::FIELD_IPC_KEY)
        {
            m_key = STRI(value);
            //DEBUG("     SPELLipcMessage key: " << m_key);
        }
        else
        {
            set( key, value );
            //DEBUG("     Key " << key << "=" << value);
        }
    }
}
