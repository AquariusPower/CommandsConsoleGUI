Hit F10 (on the test) to toggle console (initially closed).

Features:
	.Main
	= Command (and params) auto complete, key TAB, case insensitive
	= Command history, key up/down. Saved and loaded from file, see /statsShowAll.
	= Command params can be enclosed into double quotes.
	= Command hint while it is being typed.
	= If a clicked dump entry is a valid command, it will replace the input field text.
	= Batch execute initialization console commands. Configurable at file, see /statsShowAll.
	= Console style can be changed on the fly.
	= New style "console", with monospaced font, helps on proper line wrapping.
	.Nagivation
	= Navigate dump area, key pgup/pgdown or mouse scroll. Dump is saved to a file, see /statsShowAll.
	= Auto scroll after command.
	.Commands
	= Several commands available already, see command /help
	= Comment detection, line starting with such token will be ignored
	.Editing 
	= Multi-line copy: Ctrl+b mark CopyFrom "begin", Ctrl+c does CopyTo "end".
	= Single line wrap detection when copying.
	= Ctrl+del clear the input command line
	= ctrl+home sroll top, ctrl+end scroll bottom
	= Big lines are wrapped, each part ends with '\'
	= Ctrl+/ comment toggle the input line (so you can spare the command for a later time)

DONE:
	= BaseOutputFilemameOnVar
	= Log all to file. But show only enabled ones
	= Copy/paste buttons
	= when pasting text, insert the text where the cursor is...

TODO:
	+ When date changes, report on console. On startup report date on console.
	+ Optional hundredth of a second at console log.
	+ navigate thru words with ctrl+left/right
	+ optionally keep all dump logs by moving files to name datetimed ones

TODO-later:
	+ Reload from file with filters by warn/info/exception
	+ ConsStyle strTtype s=16.34 #ff4d8a0b|c=255,255,128,50|c=0.5,.85,1.0,.25 "f=Interface/Font/Console.fnt"

TODO-REALLY?:
	? EachLine1stControlCharMarker.w.e.multiline. To be used as filters
	? Easteregg(shhh!) 01f+lrtC , elosnoCpord gnitativel , nips scisyhp ylf , 01F ot revocer 
	? radioStationStream strUrl

Ps.: see the bFix... variables if you have some problem that may already have a fix here.
