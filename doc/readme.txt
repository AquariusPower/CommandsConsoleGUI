ConsoleGui: a State for JMonkeyEngine 3D, using Lemur GUI.

Hit F10 (on the test) to toggle console (initially closed).

Features:
	___Main___
	= Command (and params) auto complete, key TAB, case insensitive
	= Command history, key up/down. Saved and loaded from file, see /statsShowAll.
	= Command params can be enclosed into double quotes.
	= Command hint while it is being typed.
	= If a clicked dump entry is a valid command, it will replace the input field text.
	= Batch execute initialization console commands. Configurable at file, see /statsShowAll.
	= Console style can be changed on the fly.
	= New style "console", with monospaced font, helps on proper line wrapping.
	___Nagivation___
	= Navigate dump area, key pgup/pgdown or mouse scroll. Dump is saved to a file, see /statsShowAll.
	= Auto scroll after command.
	___Commands___
	= Several commands available already, see command /help
	= Comment detection, line starting with such token will be ignored
	= Omit the "command being run" info entry by ending a line with '#', good at init file, mainly for /echo commands.   
	___Editing___ 
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
	= ListboxSelectorClearAfterCopy

TODO.FastToImplement:
	+ UseChar "W" toSimpleGuessMaxColumns!
	+ let multiple commands in a single line separated by ';'
	+ When date(day) changes, report on console. On startup report date on console.
	+ Optional hundredth/tenth of a second at console log.
	+ optionally keep all dump logs by moving files to name datetimed ones;bKeepAllLogs.transportFlDtTimeToName
	+ Java7 (instead of 8)
	
TODO.TimeConsuming:
	.Important
	+ SeparateGUIfromCommandsManagement
	+ HK - ReflexByFieldType.singleMatchIsSafe
	.Good
	+ navigate thru words with ctrl+left/right
	+ BitmapFontBkgColor.shiftRightLeftSelect.inputField
	+ ListboxEntry(btn)FontColorWarnExceptionSelectHK

TODO.OneDay:
	+ FontCharWidthTable.SumToWrap
	+ Reload from file with filters by warn/info/exception
	+ CloneCursorMaterialForFading"blink"
	+ consoleStyle strTtype s=16.34 #ff4d8a0b|c=255,255,128,50|c=0.5,.85,1.0,.25 "f=Interface/Font/Console.fnt"
	+ FailProof (if console bugs, app must not crash): Update.tryCatch; Listeners-»listenerAction(enum,aobj); UpdtLstnrAct; DumpException(e,aobj); Init.simplyStderrPrintExceptionWithoutExitApp.retryInitWithDefaultsOnce?
	+ Extract all annotations values concerning styles attributes (see at Label.java) and auto generate a class code with final strings. 
	
TODO.REALLY???:
	? EachLine1stControlCharMarker.w.e.multiline. To be used as filters
	? Easteregg(shhh!) 01f+lrtC , elosnoCpord gnitativel , nips scisyhp ylf , 01F ot revocer 
	? radioStationStream strUrl

Ps.: having some problem? check the /fix.* commands, and add them to the Init file.
