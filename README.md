# CCcamTester scripts by Dagger
(Also called Cline, CCcline, CCcam, Camd)

Scripts for testing that a Cline works or not. Currently in Java, C# and Python.

I made them as some people host testing cline websites and reject to share their source code.

ATENTION - Currently the socket does not receive the ACK in C# and Python. 
Hope to fix it soon, but all the login process works fine as the connection is not closed.

If you need more info, here are some links that helped me to do it:

1- Some unfinished script in PHP:
  http://pastebin.com/apdmS1Be
  
2- The code of Oscam
  https://github.com/gfto/oscam/blob/253d099e7581e186432acea2b509bebd9daeffce/module-cccam.c#L43
  
3- Code of Newcam
  https://github.com/javilonas/NewBox/blob/master/cli-cccam.c
  
4- Code of Twinprot
  https://github.com/TELE-SHARE/Twinprot
  
5- Code of CSP
  http://www.infosat.org/forum/viewtopic.php?id=13
  mirror code: https://drive.google.com/open?id=0B86c-awTf5xFQUlycG1nX2xmWXM
  
There is still job to be done.

1- Fix the issue with the socket receive in C# and Python
2- Be able to test N lines of newcamd

Please, share your code!
