using System.Data.SqlTypes;
using Famix.BodyReader;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Reflection;
using System.Text;

namespace Famix
{
    public class Parser
    {
        private int _id = 1;

        private Dictionary<string, Namespace> _namespaces = new Dictionary<string, Namespace>();
        private Dictionary<string, TypeBase> _types = new Dictionary<string, TypeBase>();
        private Dictionary<string, Method>  _methods = new Dictionary<string, Method>();
        private List<Access>  _accesses = new List<Access>();
        private List<Invocation>  _invocations = new List<Invocation>();

        public event EventHandler<ProgressEventArgs> ProgressChanged;

        public void ParseAssembly(string path)
        {
            ReaderHelper.LoadOpCodes();

            _types.Clear();
            _namespaces.Clear();

            try
            {
                //Load Assembly an extract all types
                Assembly assembly = Assembly.LoadFile(path);

                OnProgressChanged("Assembly loaded successfully");

                foreach (Type type in assembly.GetTypes())
                {
                    ParseType(type);
                }

                //Es müssen erst alle Methoden erzeugt sein um dann die entsprechende Aufrufe zu finden
                foreach (Method method in _methods.Values)
                {
                    if (method.ParentType is Class)
                    {
                        method.BodyReader.GenerateInstructions();

                        foreach (Instruction instruction in method.BodyReader.Instructions)
                            ParseInstruction(instruction);
                    }
                }
            }
            catch (FileNotFoundException ex)
            {
                throw new ArgumentException("The File \"" + ex.FileName + "\" does not exist.", ex);
            }
            catch (BadImageFormatException ex)
            {
                throw new ArgumentException("The File \"" + Path.GetFileName(path) + "\" is not a valid .NET-Assembly.", ex);
            }
        }

        private void ParseInstruction(Instruction instruction)
        {
            Class cls = instruction.BodyReader.Method.ParentType as Class;

            if (cls != null)
            {
                string code = instruction.SourceCode;

                if (instruction.ContainsAttribute)
                {
                    foreach (Attribute attribute in cls.Attributes)
                    {
                        if (code.Contains(attribute.Name))
                        {
                            Access access = new Access(_id);
                            access.Accessor = instruction.BodyReader.Method;
                            access.Variable = attribute;
                            access.IsWrite = (code.Contains("stfld") || code.Contains("stsfld"));

                            _accesses.Add(access);
                            break;
                        }
                    }
                }

                if (instruction.ContainsInvocation)
                {
                    //ParseAssembly IL-Code
                    int i = code.LastIndexOf(' ');
                    string fullName = code.Substring(i).Trim();

                    if (fullName.Contains('.') && !fullName.Contains("::."))
                    {
                        Method candidates;
                        if (_methods.TryGetValue(fullName, out candidates))
                        {
                            Invocation invocation = new Invocation(_id++);
                            invocation.Sender = instruction.BodyReader.Method;
                            invocation.Candidates = candidates;
                            _invocations.Add(invocation);
                        }
                    }
                }
            }
        }

        private void ParseType(Type type)
        {
            OnProgressChanged("Extracting type \"" + type.Name + "\" in namespace \"" + type.Namespace + "\"...");

            if (type.IsClass || type.IsInterface)
            {
                TypeBase typeBase = GetOrCreateType(type);

                if (typeBase is Class)
                {
                    Class cls = (Class)typeBase;

                    OnProgressChanged("\tExtracting fields...");
                    foreach (FieldInfo field in type.GetFields(BindingFlags.NonPublic | BindingFlags.Public | BindingFlags.Static | BindingFlags.Instance))
                    {
                        Attribute attribute = new Attribute(_id++, field.ToString(), field.Name);

                        try
                        {
                            attribute.Type = GetOrCreateType(field.FieldType);
                            attribute.ParentType = cls;
                            attribute.IsPrivate = field.IsPrivate;
                            attribute.IsProtected = field.IsFamily;
                            attribute.IsInternal = field.IsAssembly;
                            attribute.IsPublic = field.IsPublic;

                            cls.Attributes.Add(attribute);
                        }
                        catch (Exception ex)
                        {
                            OnProgressChanged("EXCEPTION: " + ex.Message);
                        }
                    }
                    OnProgressChanged("\t" + cls.Attributes.Count + " fields found");

                    List<MethodBase> methodBases = new List<MethodBase>();
                    methodBases.AddRange(type.GetConstructors());
                    //methodBases.AddRange(type.GetMethods());
                    methodBases.AddRange(type.GetMethods(BindingFlags.Instance | BindingFlags.NonPublic));
                    methodBases.AddRange(type.GetMethods(BindingFlags.Instance | BindingFlags.Public | BindingFlags.Static));

                    OnProgressChanged("\tExtracting methods...");
                    foreach (MethodBase methodBase in methodBases)
                    {
                        Method method = GetOrCreateMethod(methodBase, cls);

                        method.BodyReader = new MethodBodyReader(method, methodBase);

                        cls.Methods.Add(method);
                    }
                    OnProgressChanged("\t" + type.GetMethods().Length + " methods found");
                }
            }
        }

        private Namespace GetOrCreateNamespace(string fullName)
        {
            Namespace ns;
            if (!_namespaces.TryGetValue(fullName, out ns))
            {
                int i = fullName.LastIndexOf('.');
                string name;

                Namespace parent = null;
                if (i == -1)
                    name = fullName;
                else
                {
                    name = fullName.Remove(0, i + 1);
                    parent = GetOrCreateNamespace(fullName.Remove(i));
                }

                ns = new Namespace(_id++, fullName, name);

                ns.Parent = parent;

                _namespaces.Add(fullName, ns);
            }

            return ns;
        }

        private TypeBase GetOrCreateType(Type type)
        {
            TypeBase typeBase;
            if (!_types.TryGetValue(type.FullName, out typeBase))
            {
                if (type.Name.Contains("&"))
                {
                    
                }

                if (type.IsClass || type.IsInterface)
                {
                    if (type.IsGenericType)
                        typeBase = CreateParameterizedType(type);
                    else
                        typeBase = new Class(_id++, type.FullName, type.Name);

                    Class cls = (Class) typeBase;

                    cls.Anchor = new Anchor(_id++);
                    cls.Anchor.StartLine = 0;
                    cls.Anchor.EndLine = 0;
                    cls.Anchor.FileName = cls.Name;
                    cls.Anchor.Element = cls;

                    cls.IsInterface = type.IsInterface;
                }
                else if (type.IsEnum)
                    typeBase = CreateEnum(type);
                else if (type.IsPrimitive || type.IsValueType)
                    typeBase = new PrimitiveType(_id++, type.FullName, type.Name);

                if(typeBase == null)
                    throw new ArgumentException("Type \"" + type.Name + "\" not accepted!");

                typeBase.IsPrivate = type.IsNotPublic;
                typeBase.IsProtected = type.IsNestedFamily;
                typeBase.IsPublic = type.IsPublic;

                typeBase.Namespace = GetOrCreateNamespace((string.IsNullOrEmpty(type.Namespace) ? "Empty" : type.Namespace));

                if (type.BaseType != null)
                    typeBase.BaseType = GetOrCreateType(type.BaseType);

                _types.Add(typeBase.FullName, typeBase);
            }

            return typeBase;
        }

        private ParameterizedType CreateParameterizedType(Type type)
        {
            ParameterizedType pt = new ParameterizedType(_id++, type.FullName, type.Name);

            foreach (Type argType in type.GenericTypeArguments)
                pt.Arguments.Add(GetOrCreateType(argType));

            //get or create ParameterizedClass (without generic arguments)
            string pcName = type.Name.Substring(0, type.Name.IndexOf('`'));
            string pcFullName = type.FullName.Substring(0, type.FullName.IndexOf('`'));

            TypeBase pc;
            if (!_types.TryGetValue(pcName, out pc))
            {
                pc = new ParameterizedClass(_id++, pcFullName, pcName);

                pc.Namespace = GetOrCreateNamespace((type.Namespace));

                pc.IsPrivate = type.IsNotPublic;
                pc.IsProtected = type.IsNestedFamily;
                pc.IsPublic = type.IsPublic;

                ((Class)pc).IsInterface = type.IsInterface;

                _types.Add(pcName, pc);
            }

            pt.ParameterizableClass = (ParameterizedClass)pc;

            return pt;
        }

        private Enum CreateEnum(Type type)
        {
            Enum e = new Enum(_id++, type.FullName, type.Name);

            foreach (var v in type.GetEnumValues())
            {
                EnumValue ev = new EnumValue(_id++, e.FullName + "." + v, v.ToString());
                ev.ParentEnum = e;
                e.Values.Add(ev);
            }

            return e;
        }

        private Method GetOrCreateMethod(MethodBase methodBase, TypeBase typeBase = null)
        {
            //Generate fullname with type, method fullName and paremter types
            string paramString = string.Empty;
            List<Parameter> parameters = new List<Parameter>();
            foreach (var parameterInfo in methodBase.GetParameters())
            {
                Parameter parameter = new Parameter(parameterInfo.Name);
                parameter.Type = GetOrCreateType(parameterInfo.ParameterType);

                paramString += parameter.Type.FullName + ", ";

                parameters.Add(parameter);
            }

            if (paramString.Length > 2)
                paramString = paramString.Remove(paramString.Length - 2);

            string fullName;
            if (methodBase.DeclaringType == null)
                fullName = methodBase.Name;
            else
            {
                fullName = methodBase.DeclaringType.FullName + "::" + methodBase.Name;
                fullName += "(" + paramString + ")";
            }

            Method method;
            if (!_methods.TryGetValue(fullName, out method))
            {
                method = new Method(_id++, fullName, methodBase.Name);

                method.IsPrivate = methodBase.IsPrivate;
                method.IsProtected = methodBase.IsFamily;
                method.IsInternal = methodBase.IsAssembly;
                method.IsPublic = methodBase.IsPublic;
                method.IsAbstract = methodBase.IsAbstract;

                method.Anchor = new Anchor(_id++);
                method.Anchor.StartLine = 0;
                method.Anchor.EndLine = 0;
                method.Anchor.FileName = methodBase.DeclaringType.Name;
                method.Anchor.Element = method;

                if (typeBase == null)
                    method.ParentType = GetOrCreateType(methodBase.DeclaringType);
                else
                    method.ParentType = typeBase;

                if (methodBase is ConstructorInfo)
                    method.IsConstructor = true;

                method.Parameters.AddRange(parameters);

                try
                {
                    if (methodBase is MethodInfo)
                        method.Type = GetOrCreateType(((MethodInfo) methodBase).ReturnType);
                }
                catch (Exception ex)
                {
                    OnProgressChanged("EXCEPTION: " + ex.Message);
                }

                _methods.Add(method.FullName, method);
            }

            return method;
        }

        public void Save(string filename)
        {
            StringBuilder sb = new StringBuilder();

            sb.Append('(');

            foreach (Namespace ns in _namespaces.Values)
               ns.AddFamix(sb);

            foreach (TypeBase type in _types.Values)
                type.AddFamix(sb);

            //Vererbung
            foreach (TypeBase type in _types.Values.ToList().Where(tb => tb.BaseType != null))
            {
                sb.AppendLine();
                sb.Append("\t(FAMIX.Inheritance (id: " + _id++ + ")");
                sb.AppendLine();
                sb.Append("\t\t(subclass (ref: " + type.Id + "))");
                sb.AppendLine();
                sb.Append("\t\t(superclass (ref: " + type.BaseType.Id + ")))");
            }

            foreach (Class cls in _types.Values.Where(tb => tb is Class).Cast<Class>())
            {
                foreach (Attribute attribute in cls.Attributes)
                    attribute.AddFamix(sb);
            }

            foreach (Method method in _methods.Values)
                method.AddFamix(sb);

            foreach (Access access in _accesses)
                access.AddFamix(sb);

            foreach (Invocation invocation in _invocations)
                invocation.AddFamix(sb);

            sb.AppendLine();
            sb.Append(')');

            sb.Replace("&", "_");
            sb.Replace("<", "_");
            sb.Replace(">", "_");
            File.WriteAllText(filename, sb.ToString());
            string path = Path.GetFullPath(filename);
            Console.WriteLine("filepath" + path);

            OnProgressChanged("File saved");
        }

        private void OnProgressChanged(string message)
        {
            if(ProgressChanged != null)
                ProgressChanged(this, new ProgressEventArgs(message));
        }
    }
}