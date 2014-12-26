/*

Console Internal Commands (CICs) are handled with the matching cmd name (not case sensitive)
Overwrite the public methods of these cmd would change the behavior of CICs
Simply adding an else-if clause will not solve the problem since CIC-handling starts from the base

When a new CIC is defined, make sure all the CIC logic is located in a method with the name of the specific CIC

 */

import java.util.*;

public class Console
{
    protected ArrayList<String> currentDirectory = new ArrayList<String>();
    protected String bufferedDirectory = "";
    protected String dirSep = "/";
    protected Scanner stdin = new Scanner(System.in);
    protected boolean isTerminated = false;

    public Console(String dirSep)
    {
    	this.dirSep = dirSep;
    }

    public Console()
    {
        this("/");
        makeDirectoryBuffer();
    }

    public static String join(ArrayList<String> r, String d)
    {
        if (r.size() == 0) return "";
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for(i=0; i<r.size()-1; i++)
            sb.append(r.get(i)+d);
        return sb.toString()+r.get(i);
    }

    protected void makeDirectoryBuffer()
    {
        if (!currentDirectory.isEmpty()) bufferedDirectory = dirSep + join(currentDirectory, dirSep)+">";
        else bufferedDirectory = dirSep+">";
    }

    protected void popDirectoryElement()
    {
        if (!currentDirectory.isEmpty())
        {
            currentDirectory.remove(currentDirectory.size() - 1);
        }
    }

    public int cd(String param)
    {
        if (param.isEmpty()) return 0;
        if (param.equals(".."))
        {
            popDirectoryElement();
        }
        else
        {
            if (param.charAt(0) == '"' && param.charAt(param.length()-1) == '"')    //path specifier
            {
                param = param.subSequence(1, param.length()-1).toString();
            }
            String[] path;
            if (param.indexOf(dirSep) == 0)
            {
                currentDirectory.clear();
                param = param.substring(dirSep.length());
            }
            if (param.contains(dirSep)) path = param.split(dirSep);
            else path = new String[]{param};

            for (String str: path)
            {
                if (str.equals("."))
                {

                }
                else if (str.equals(".."))
                {
                    popDirectoryElement();
                }
                else if (!str.isEmpty())
                {
                    currentDirectory.add(str);
                }
            }
        }
        makeDirectoryBuffer();
    	return 0;
    }

    public static enum EXCEPTION
    {
        Exception, Error, InvalidParameterException, InvalidCommandException
    }

    public void Exception(String expInfo)
    {
        Exception(EXCEPTION.Exception, expInfo);
    }

    public void Exception(EXCEPTION expType, String expInfo)
    {
        switch(expType)
        {
        case Exception:
            System.out.println("An Exception Occured During the Handling Procedure.  ");
            break;
        case Error:
            System.out.println("Error");
            break;
        case InvalidParameterException:
            System.out.println("InvalidParameterException");
            break;
        case InvalidCommandException:
            System.out.println("InvalidCommandException");
            break;
        }
        System.out.println(expInfo);
    }

    public void Exception(EXCEPTION exptype)
    {
        Exception(exptype, "");
    }

    public void Exception()
    {
        Exception(EXCEPTION.Exception);
    }

    public int exit(String param)   //is exiting?
    {
        return exit();
    }

    public int exit()
    {
        isTerminated = true;
        return 0;
    }

    private int lastResult = 0;
    public int getLastResult()
    {
        return lastResult;
    }

    public static final int CMD_SUCCESS = 0;
    public static final int CMD_RETURN_VOID = -1;
    public static final int CMD_NOT_HANDLED = -2;
    protected int inputHandler(String cmd, String param)
    /*
    This Method Should Be Overwritten.
    This Method Should NOT Be Called Directly.  (Call inputHandler(String) instead)

    return:
        -1: handled cmd, cmd return type is VOID
        -2: not handled cmd, could be handled in the overwritten methods
        anythingelse: cmd-specific results
    sample:
    <CODE>
        class ConsoleTest extends Console
    {
        protected int inputHandler(String cmd, String param)
        {
            int result = super.inputHandler(cmd, param);
            if (result != CMD_NOT_HANDLED) return result;

            result = CMD_RETURN_VOID;
            if (cmd.equals("cd"))
            {
                result = cd(param);
            } else if (cmd.equals("exit"))
            {
                exit(param);
            } else
            {
                return CMD_NOT_HANDLED;  //not handled
            }
            return result;
        }
    }
    </CODE>
     */
    {
        return CMD_NOT_HANDLED;
    }

    private boolean _inputHandler(String cmd, String param)    //return true only if handled this cmd
    {
        lastResult = inputHandler(cmd, param);
        return true;
    }

    public boolean inputHandler(String userInput)
    {
        String[] arr = parseInput(userInput);
        return _inputHandler(arr[0], arr[1]);
    }

    protected String[] parseInput(String userInput)
    {
        String[] arr = userInput.split(" ");
        String[] result = new String[2];
        result[0] = arr[0].toLowerCase();
        result[1] = userInput.substring(arr[0].length()).trim();
        return result;
    }

    public static void main(String[] args)
    {
        ConsoleTest cl = new ConsoleTest();
        while (cl.next());
    }

    public boolean next()
    {
        if (!isTerminated)
        {
            System.out.print(bufferedDirectory);
            inputHandler(stdin.nextLine());
            return !isTerminated;
        }
        return false;
    }
}

class ConsoleTest extends Console
{
    protected int inputHandler(String cmd, String param)
    {
        int result = super.inputHandler(cmd, param);
        if (result != CMD_NOT_HANDLED) return result;

        result = CMD_RETURN_VOID;
        if (cmd.equals("cd"))
        {
            result = cd(param);
        } else if (cmd.equals("exit"))
        {
            exit(param);
        } else
        {
            return CMD_NOT_HANDLED;  //not handled
        }
        return result;
    }
}