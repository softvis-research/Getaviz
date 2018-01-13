using System.Collections.Generic;
using System.Text;

namespace Famix
{
    public class ParameterizedType : Class
    {
        public ParameterizedType(int id, string fullName, string name)
            : base(id, fullName, name)
        {
            Arguments = new List<TypeBase>();
        }

        internal override void AddFamix(StringBuilder sb)
        {
            sb.AppendLine();
            sb.Append("\t(FAMIX.ParameterizedType");
            AddId(sb);
            AddName(sb);

            if (Arguments.Count > 0)
            {
                sb.AppendLine();
                sb.Append("\t\t(arguments (ref: " + Arguments[0].Id + "))");
            }

            AddNamespace(sb);
            sb.AppendLine();
            sb.Append("\t\t(isStub true)");
            sb.AppendLine();
            sb.Append("\t(parameterizableClass (ref: " + ParameterizableClass.Id + "))");
            sb.Append(')');
        }

        public List<TypeBase> Arguments { get; private set; }
        public ParameterizedClass ParameterizableClass { get; internal set; }
    }
}
