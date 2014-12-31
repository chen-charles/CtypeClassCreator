import java.io.*;
import java.util.*;
import java.security.*;
import java.math.*;

interface CtypeObject
{
	public String generateH(String classname);
	public String generateC(String classname);
	public String toString();
	public String md5(String s) throws NoSuchAlgorithmException;
}

abstract class _CtypeObject implements CtypeObject, Serializable
{
	private static int ID = 0;
	public final int uID = ID++;
	public String md5(String s) throws NoSuchAlgorithmException
	{
		MessageDigest m = MessageDigest.getInstance("MD5");
		m.update(s.getBytes(), 0, s.length());
		BigInteger i = new BigInteger(1,m.digest());
		return String.format("%1$032x", i);
	}
}

public class CtypeClassObject implements Serializable
{
	public ArrayList<String> dependency = new ArrayList<String>();
	public ArrayList<Variable> var = new ArrayList<Variable>();
	public ArrayList<Method> method = new ArrayList<Method>();
	public static final String HFormat = "#ifndef __%s__\n#define __%s__\n#include <stdlib.h>\n\n%s\n\n#endif\n";
	public static final String CFormat = "#include \"%s\"\n%s\n";
	public static final String VFormat = "typedef struct %s\n{\n\t%s\n};\n\n";

	public String name = "noname";
	public void rename(String name){this.name = name;}

	public CtypeClassObject()
	{
	}

	public void var(String type, String name)
	{
		this.var(new Variable(type, name));
	}

	public void var(Variable var)
	{
		for (Variable i: this.var)
		{
			if (i.type.equals(var.type) && i.name.equals(var.name)) return; //identical var decl, fail
		}
		this.var.add(var);
	}

	public void method(Method method)
	{
		for (Method i: this.method)
		{
			if (i.type.equals(method.type) && i.name.equals(method.name) && i.args.size() == method.args.size())
			{
				//furthur confirmation
				boolean isFailed = true;
				for (int j=0; j<i.args.size(); j++)
				{
					isFailed = true;
					if (!i.args.get(j).type.equals(method.args.get(j).type))
					{
						isFailed = false;
					}
					if (!isFailed) break;
				}
				if (isFailed) return;   //failed, params are all same type
			}
		}
		this.method.add(method);
	}

	public String generateH()
	{
		StringBuilder dep = new StringBuilder();
		for (String i: this.dependency)
		{
			dep.append(String.format("#include\t\"%s\"\n", i));
		}

		StringBuilder sb = new StringBuilder();
//		sb.append("\n\n//Variables");
//		for (Variable i: this.var)
//		{
//			sb.append(String.format("\n%s* %s_%s(%s* this);\n", i.type, name, i.name, name+"_VAR"));
//		}
		sb.append("\n\n//Methods");
		sb.append(String.format("\n%s_VAR* %s();\n", name, name));
		return String.format(HFormat, name, name, dep.toString() + joinVar(this.var) + joinH(this.method, "\n")
				+ sb.toString());

	}

	public String generateC()
	{
		StringBuilder sb = new StringBuilder();
//		sb.append("\n\n//Variables");
//		for (int i=0; i<this.var.size(); i++)
//		{
//			Variable j = this.var.get(i);
//			String s = String.format("(%s*)(&(this->%s))", j.type, j.name);
//			sb.append(String.format("\n%s* %s_%s(%s* this){return %s;}\n", j.type, name, j.name, name+"_VAR", s));
//		}
		sb.append("\n\n//Methods");
		sb.append(String.format("\n%s_VAR* %s(){return (%s_VAR*)malloc(sizeof(%s_VAR));}\n", name, name, name, name));
		return String.format(CFormat, name+".h", sb.toString() + joinC(this.method, "\n"));
	}

	public String joinVar(ArrayList<Variable> r)
	{
		return String.format(VFormat, name+"_VAR", joinH(this.var, "\n\t"));
	}

	public String joinH(ArrayList r, String d)
	{
		if (r.size() == 0) return "";
		StringBuilder sb = new StringBuilder();
		int i = 0;
		for(i=0; i<r.size()-1; i++)
			sb.append(((CtypeObject)(r.get(i))).generateH(name)+d);
		return sb.toString()+((CtypeObject)(r.get(i))).generateH(name);
	}

	public String joinC(ArrayList r, String d)
	{
		if (r.size() == 0) return "";
		StringBuilder sb = new StringBuilder();
		int i = 0;
		for(i=0; i<r.size()-1; i++)
			sb.append(((CtypeObject)(r.get(i))).generateC(name)+d);
		return sb.toString()+((CtypeObject)(r.get(i))).generateC(name);
	}
}

class Method extends _CtypeObject implements Serializable
{
	public String name;
	public String type;
	public ArrayList<Variable> args;
	public Method(String type, String name, ArrayList<Variable> args)
	{
		this.name = name;
		this.type = type;
		this.args = args;
	}

	public ArrayList<Variable> getArgs()
	{
		return args;
	}

	public String toString()
	{
		return String.format("%d %s %s(%s)", this.uID, type, name, join(args, ", "));
	}

	public String generateH(String classname)
	{
		return this.generate(classname) + ";";
	}

	public String join(ArrayList<Variable> r, String d)
	{
		if (r.size() == 0) return "";
		StringBuilder sb = new StringBuilder();
		int i = 0;
		for(i=0; i<r.size()-1; i++)
			sb.append(((r.get(i))).toString_m()+d);
		return sb.toString()+((r.get(i))).toString_m();
	}

	public String generate(String classname)
	{
		return String.format("%s %s_%s(%s_VAR* this, %s)", type, classname, name, classname, join(args, ", "));
	}

	public String generateC(String classname)
	{
		return this.generate(classname) + "{}\n";
	}
}

class Variable extends _CtypeObject implements Serializable
{
	public String name;
	public String type;
	public Variable(String type, String name)
	{
		this.name = name;
		this.type = type;
	}

	public String toString()
	{
		return String.format("%d %s %s", this.uID, type, name);
	}

	public String toString_m()
	{
		return String.format("%s %s", type, name);
	}

	public String generateH(String classname)
	{
		return String.format("%s %s;", type, name);
	}

	public String generateC(String classname)
	{
		return "";
	}
}