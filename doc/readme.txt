ConsoleGui: a State for JMonkeyEngine 3D, using Lemur GUI.

WIP: Work in progress!

Hit F10 (on the test) to toggle console (initially closed).

Features:
	___Main___
	= Command (and params) auto complete "startWith", key TAB, case insensitive
	= Command (and params) auto complete "contains" , key Ctrl+TAB, case insensitive
	= Command history, key up/down. Saved and loaded from file, see /statsShowAll.
	= Command params can be enclosed into double quotes.
	= Command hint (Listbox) while it is being typed.
	= Minimal scripting using console commands: variables, conditional blocks etc.
	= If a clicked dump entry is a valid command, it will replace the input field text (that will be dumped).
	= Batch execute initialization console commands. Configurable at file, see /statsShowAll.
	= Console style can be changed on the fly.
	= Line wrapping works for all styles.
	___Nagivation___
	= Navigate dump area, key pgup/pgdown or mouse scroll. Dump is saved to a file, see /statsShowAll.
	= Auto scroll after command.
	___Commands___ (see /help)
	= Alias to run multiple commands.
	= Variables can be set and evaluated to run console commands.
	= Most commands will have an identical variable identifier holding its setup value. Usage ex.: /echo ${&consoleHeight}
	= Comment token detection, after it, line is ignored.
	= Omit the "command being run" info entry by ending a line with '#', good at init file, mainly for /echo commands.
	= Multi-line conditional command blocks using: if, elseIf, else, ifEnd (can be nested)
	= Console functions can be created and run with parameters.
	___Editing___ 
	= Multi-line copy: Ctrl+b marks CopyFromIndex "begin", Ctrl+c/x marks CopyToIndex "end", Shift+click stores the previous CopyToIndex into CopyFromIndex.
	= Single line wrap detection when copying.
	= Ctrl+del clear the input command line
	= Ctrl+home sroll top, Ctrl+end scroll bottom
	= Big lines are wrapped, each part ends with '\'
	= Ctrl+/ comment toggle on the input line.
	= Any changes to the input text will reset the command history cursor, hit Down to access your last non issued command.

NAMING CONVENTION:
	. Methods ending with TS are synchronized/threadSafe, it overrides TD idea below.
	. Methods ending with TD will be running in a separate (other than main) thread, therefore they go better if only directly call TS methods.
	. Classes ending with I have only one instance and a static method to access it.
	. Classes ending with Data have many instances used on lists etc.

DONE:
	= BaseOutputFilemameOnClass
	= Log all to file. But show only enabled ones
	= Copy/paste buttons
	= when pasting text, insert the text where the cursor is...
	= ListboxSelectorClearAfterCopy
	= Java7 compliance (instead of 8)
	= FIX: Copy from/to is inverted concerning SHIFT key. Holding shift must mark copyTo.
	= let multiple commands in a single line separated by ';'
	= Alias -tst;Alias +tst;CMD_TEST_ABC reflex testAbc
	= DB hash last save
	= ToggleAutoBkpIfDBhashChanges
	= Setup.cfg.areCchangesMadeIngame.overridenBy.Init.cfg
	= If true, Exec all lines til Else.ElseIf.IfEnd; If.if. Nested index. Requires nested endings;If false, will skip subsequent commands til else or end.
	= Alias/var autocomplete, also in the middle of the input field text.  
	= PreCommandsQueue: sends commands to queue, but if a /sleep command is found, this dispatching will be delayed. Sleep is only allowed on a list of commands sent to queue.
	= Scroll by mouse location: over dump area, over hint area or over input field cmd history (input field optional toggle).
	= Console is properly initially hidden now (cullhint)
	= /sleep 2.5 [cmd]; allow /sleep to exec commands too, after the delay; this way it will not prevent next commands from being executed, and can be used outside of cmds block
	= auto-scroll toggle button/option.
	= toggle run console commands in background (with console closed)
	= commands setup value can be retrieved like: ${&consoleHeight}
	= transform TimedDelay main value into varible like BoolTogglers!
	+ navigate thru words with ctrl+left/right

DOING:
	... SeparateGUI class from Commands Management, independent from Lemur and JME, so can be used even on a text console or 2D application etc.
	... SeparateGUI class from Lemur, so other GUI can use the same abstract class to implement a console GUI.

TODO.FastToImplement:
	+ Auto complete with aliases if line begins with alias token.
	+ /bind [--list]|[<key> <cmd>]; simplify key to accept lowercase and be as shortest as possible
	+ /while stores subsequent commands on array for repeating til WhileEnd. Nestable too.
	+ AtSetup:windowGeometry.resolution.position;keybinds.sndVplumes;etc
	+ When date(day) changes, report on console. On startup report date on console.
	+ Optional hundredth/tenth of a second at console log.
	+ optionally keep all dump logs by moving files to name datetimed ones;bKeepAllLogs.transportFlDtTimeToName
	+ stats displayed in buttons, click each to expand/shrink.
	
TODO.TimeConsuming:
	.Good
	+ BitmapFontBkgColor.shiftRightLeftSelect.inputField
	+ ListboxEntry(btn)FontColorWarnExceptionSelectHK
	+ Separate scripting commands in another class, so they can be fully disabled by not instancing it.

TODO.OneDay:
	+ FontCharWidthTable.SumToWrap
	+ Reload from file with filters by warn/info/exception
	+ CloneCursorMaterialForFading"blink"
	+ consoleStyle strTtype s=16.34 #ff4d8a0b|c=255,255,128,50|c=0.5,.85,1.0,.25 "f=Interface/Font/Console.fnt"
	+ FailProof (if console bugs, app must not crash): Update.tryCatch; Listeners-»listenerAction(enum,aobj); UpdtLstnrAct; DumpException(e,aobj); Init.simplyStderrPrintExceptionWithoutExitApp.retryInitWithDefaultsOnce?
	+ Extract all annotations values concerning styles attributes (see at Label.java) and auto generate a class code with final strings. 
	+ HK - ReflexByFieldType.singleMatchIsSafe.insteadOfSolelyByFieldName.MayBeThrowTheOptionsInConsoleForUserToSet
	
TODO.REALLY???:
	? EachLine1stControlCharMarker.w.e.multiline. To be used as filters
	? Easteregg(shhh!) 01f+lrtC , elosnoCpord gnitativel , nips scisyhp ylf , 01F ot revocer 
	? radioStationStream strUrl

Ps.: having some problem? check the /fix.* commands, and add them to the Init file.
