# CCcamTester scripts by Dagger
Scripts for testing that a Cline works or not. Currently in C# and Python.

I made them to PISS OFF those people that don't share their source code and host testing cline websites so they could steal them. (You REALLY suck guys)

ATENTION - Currently the socket does not receive the ACK. Hope to fix it soon, but all the login process works fine and tells you if you've entered a wrong username/psw. The problem is that once is connected stays like that and the server does not reply.
Still, it works as if you enter a wrong username or password the connection is closed.

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

1- Fix the issue with the socket receive

2- Be able to test N lines of newcamd

Please, don't be an asshat and share your code!
