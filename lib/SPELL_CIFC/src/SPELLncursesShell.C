// ################################################################################
// FILE       : SPELLncursesShell.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the NCurses shell for standalone executor
// --------------------------------------------------------------------------------
//
//  Copyright (C) 2008, 2018 SES ENGINEERING, Luxembourg S.A.R.L.
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
// Project includes --------------------------------------------------------
#include "SPELL_EXC/SPELLexecutor.H"
#include "SPELL_EXC/SPELLcommand.H"
#include "SPELL_UTIL/SPELLpythonHelper.H"
// Local includes ----------------------------------------------------------
#include "SPELL_CIFC/SPELLncursesShell.H"


// DEFINES /////////////////////////////////////////////////////////////////
#define ARRAY_SIZE(a) (sizeof(a) / sizeof(a[0]))
#define LINE_START 50
#define LOG_LINE_START 10

#define NUM_LINES  39
#define NUM_LOG_LINES 8

#define COLOR_ERROR   1
#define COLOR_HIGH    2
#define COLOR_WARNING 3
#define COLOR_OK      4

static const std::string choices[] = {
    "RUN",
    "PAUSE",
    "SKIP",
    "STEP",
    "STEP OVER",
    "GOTO LINE",
    "GOTO LABEL",
    "SCRIPT",
    "ABORT",
    "RELOAD"
};

static const std::string descriptions[] = {
    "Run the procedure",
    "Pause the execution",
    "Skip current line",
    "Execute current line",
    "Execute current line (do not go into it)",
    "Go to a line number",
    "Go to a label",
    "Execute script",
    "Abort execution",
    "Reload procedure"
};

//=============================================================================
// CONSTRUCTOR: SPELLncursesShell::SPELLncursesShell
//=============================================================================
SPELLncursesShell::SPELLncursesShell()
    : SPELLthread("shell")
{
    m_ready = false;
    setupCurses();
    setupMenu();
}

//=============================================================================
// DESTRUCTOR: SPELLncursesShell:SPELLncursesShell
//=============================================================================
SPELLncursesShell::~SPELLncursesShell()
{
    m_messages.clear();
    m_logs.clear();
}

//=============================================================================
// METHOD: SPELLncursesShell:shouldFinish
//=============================================================================
bool SPELLncursesShell::shouldFinish( int c )
{
    if ( c == KEY_F(1) )
    {
        SPELLexecutorStatus st = SPELLexecutor::instance().getStatus();
        if (st == STATUS_ABORTED || st == STATUS_FINISHED || st == STATUS_ERROR )
        {
            return true;
        }
    }
    return false;
}

//=============================================================================
// METHOD: SPELLncursesShell:run
//=============================================================================
void SPELLncursesShell::run()
{
    int c;
    m_ready = true;
    m_closeEvent.clear();
    while(true)
    {
        c = wgetch(m_menuWin);
        if (shouldFinish(c)) break;
        switch(c)
        {
        case KEY_DOWN:
            menu_driver(m_menu, REQ_DOWN_ITEM);
            break;
        case KEY_UP:
            menu_driver(m_menu, REQ_UP_ITEM);
            break;
        case KEY_LEFT:
            menu_driver(m_menu, REQ_LEFT_ITEM);
            break;
        case KEY_RIGHT:
            menu_driver(m_menu, REQ_RIGHT_ITEM);
            break;
        case KEY_NPAGE:
            menu_driver(m_menu, REQ_SCR_DPAGE);
            break;
        case KEY_PPAGE:
            menu_driver(m_menu, REQ_SCR_UPAGE);
            break;
        case 10: // Enter
        {
            ITEM *cur;
            cur = current_item(m_menu);
            std::string name = item_name(cur);
            if (name == choices[0])
            {
                command(CMD_RUN);
            }
            else if (name == choices[1])
            {
                command(CMD_PAUSE);
            }
            else if (name == choices[2])
            {
                command(CMD_SKIP);
            }
            else if (name == choices[3])
            {
                command(CMD_STEP);
            }
            else if (name == choices[4])
            {
                command(CMD_STEP_OVER);
            }
            else if (name == choices[5])
            {
                //command(CMD_GOTO);
            }
            else if (name == choices[6])
            {
                //command(CMD_GOTO);
            }
            else if (name == choices[7])
            {
                //command(CMD_SCRIPT);
            }
            else if (name == choices[8])
            {
                command(CMD_ABORT);
            }
            else if (name == choices[9])
            {
                command(CMD_RELOAD);
            }
            break;
        }
        }
        refresh();
        wrefresh(m_menuWin);
    }
    m_ready = false;
    m_closeEvent.set();
}

//=============================================================================
// METHOD: SPELLncursesShell:waitClose
//=============================================================================
bool SPELLncursesShell::waitClose()
{
    m_closeEvent.wait();
    /** \todo shall return true on reload */
    return false;
}

//=============================================================================
// METHOD: SPELLncursesShell:show_stack
//=============================================================================
void SPELLncursesShell::show_stack( std::string msg )
{
    if (!m_ready) return;
    mvprintw(LINES - 52, 3,"LINE:");
    mvprintw(LINES - 52, 12, msg.c_str());
    refresh();
}

//=============================================================================
// METHOD: SPELLncursesShell:show_status
//=============================================================================
void SPELLncursesShell::show_status( std::string status )
{
    if (!m_ready) return;
    mvprintw(LINES - 53, 3,"STATUS:");
    int cpair = COLOR_OK;
    if (status == "ERROR")
    {
        cpair = COLOR_ERROR;
    }
    else if (status == "FINISHED")
    {
        cpair = COLOR_HIGH;
    }
    else if (status == "ABORTED")
    {
        cpair = COLOR_ERROR;
    }
    attron(COLOR_PAIR(cpair));
    mvprintw(LINES - 53, 12, status.c_str());
    attroff(COLOR_PAIR(cpair));
    refresh();
}

//=============================================================================
// METHOD: SPELLncursesShell:show_info
//=============================================================================
void SPELLncursesShell::show_info( std::string message )
{
    if (!m_ready) return;
    m_messages.push_back(message);
    m_mtypes.push_back(0);
    if (m_messages.size()>NUM_LINES)
    {
        m_messages.erase( m_messages.begin() );
        m_mtypes.erase( m_mtypes.begin() );
    }
    updateMessages();
}

//=============================================================================
// METHOD: SPELLncursesShell:show_warning
//=============================================================================
void SPELLncursesShell::show_warning( std::string message )
{
    if (!m_ready) return;
    m_messages.push_back(message);
    m_mtypes.push_back(1);
    if (m_messages.size()>NUM_LINES)
    {
        m_messages.erase( m_messages.begin() );
        m_mtypes.erase( m_mtypes.begin() );
    }
    updateMessages();
}

//=============================================================================
// METHOD: SPELLncursesShell:log
//=============================================================================
void SPELLncursesShell::log( std::string message )
{
    if (!m_ready) return;
    m_logs.push_back(message);
    if (m_logs.size()>NUM_LOG_LINES)
    {
        m_logs.erase( m_logs.begin() );
    }
    updateLog();
}

//=============================================================================
// METHOD: SPELLncursesShell:show_error
//=============================================================================
void SPELLncursesShell::show_error( std::string message )
{
    if (!m_ready) return;
    m_messages.push_back(message);
    m_mtypes.push_back(2);
    if (m_messages.size()>NUM_LINES)
    {
        m_messages.erase( m_messages.begin() );
        m_mtypes.erase( m_mtypes.begin() );
    }
    updateMessages();
}

//=============================================================================
// METHOD: SPELLncursesShell:setupCurses
//=============================================================================
void SPELLncursesShell::setupCurses()
{
    // Initialize curses
    initscr();
    start_color();
    cbreak();
    noecho();
    keypad(stdscr, TRUE);
    init_pair(COLOR_ERROR, COLOR_RED, COLOR_BLACK);
    init_pair(COLOR_HIGH, COLOR_CYAN, COLOR_BLACK);
    init_pair(COLOR_WARNING, COLOR_YELLOW, COLOR_BLACK);
    init_pair(COLOR_OK, COLOR_GREEN, COLOR_BLACK);
}

//=============================================================================
// METHOD: SPELLncursesShell:setupMenu
//=============================================================================
void SPELLncursesShell::setupMenu()
{
    // Create menu items
    int n_choices, i;
    n_choices = ARRAY_SIZE(choices);
    m_menuItems = (ITEM **)calloc(n_choices, sizeof(ITEM *));
    for(i = 0; i < n_choices; ++i)
    {
        m_menuItems[i] = new_item(choices[i].c_str(), descriptions[i].c_str());
    }

    // Crate menu
    m_menu = new_menu((ITEM **)m_menuItems);

    // Set menu option not to show the description
    menu_opts_off(m_menu, O_SHOWDESC);

    // Create the window to be associated with the menu
    m_menuWin = newwin(4, 79, 1, 1);
    keypad(m_menuWin, TRUE);

    // Set main window and sub window
    set_menu_win(m_menu, m_menuWin);
    set_menu_sub(m_menu, derwin(m_menuWin, 3, 77, 1, 1));
    set_menu_format(m_menu, 2, 5);
    set_menu_mark(m_menu, "*");

    // Print a border around the main window and print a title
    box(m_menuWin, 0, 0);

    attron(COLOR_PAIR(2));
    mvprintw(LINES - 1, 0, "Use Arrow Keys to navigate (F1 to Exit)");
    attroff(COLOR_PAIR(2));

    // Refresh the screen
    refresh();

    // Post the menu and refresh the window
    post_menu(m_menu);
    wrefresh(m_menuWin);
}

//=============================================================================
// METHOD: SPELLncursesShell:cleanupMenu
//=============================================================================
void SPELLncursesShell::cleanupMenu()
{
    unpost_menu(m_menu);
    for(unsigned int i = 0; i<ARRAY_SIZE(choices); i++)
    {
        free_item(m_menuItems[i]);
    }
    free_menu(m_menu);
    delwin(m_menuWin);
}

//=============================================================================
// METHOD: SPELLncursesShell:cleanup
//=============================================================================
void SPELLncursesShell::cleanup()
{
    cleanupMenu();
    endwin();
    m_ready = false;
}

//=============================================================================
// METHOD: SPELLncursesShell:updateMessages
//=============================================================================
void SPELLncursesShell::updateMessages()
{
    attron(COLOR_PAIR(4));
    for( unsigned int line = 0; line < 30; line++ )
    {
        mvprintw(LINES-LINE_START + line, 0, "                                                                                                ");
    }
    for( unsigned int line = 0; line < m_messages.size(); line++ )
    {
        switch(m_mtypes[line])
        {
        case 0:
            attron(COLOR_PAIR(COLOR_OK));
            break;
        case 1:
            attron(COLOR_PAIR(COLOR_WARNING));
            break;
        case 2:
            attron(COLOR_PAIR(COLOR_ERROR));
            break;
        }
        mvprintw(LINES-LINE_START + line, 0, (std::string(" ",70)).c_str());
        mvprintw(LINES-LINE_START + line, 0, m_messages[line].c_str());
        switch(m_mtypes[line])
        {
        case 0:
            attroff(COLOR_PAIR(COLOR_OK));
            break;
        case 1:
            attroff(COLOR_PAIR(COLOR_WARNING));
            break;
        case 2:
            attroff(COLOR_PAIR(COLOR_OK));
            break;
        }
    }
    refresh();
}

//=============================================================================
// METHOD: SPELLncursesShell:updateLog
//=============================================================================
void SPELLncursesShell::updateLog()
{
    for( unsigned int line = 0; line < m_logs.size(); line++ )
    {
        mvprintw(LINES-LOG_LINE_START + line, 0, (std::string(" ",70)).c_str());
        mvprintw(LINES-LOG_LINE_START + line, 0, m_logs[line].c_str());
    }
    refresh();
}

//=============================================================================
// METHOD: SPELLncursesShell:command
//=============================================================================
void SPELLncursesShell::command( std::string cmdId )
{
    log("Commmand " + cmdId + " sent");
    ExecutorCommand cmd;
    cmd.id = cmdId;
    SPELLexecutor::instance().command(cmd, true);
}
