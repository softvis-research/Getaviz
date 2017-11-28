using System.Text;

namespace Famix
{
    public class ParameterizedClass : Class
    {
        public ParameterizedClass(int id, string fullName, string name)
            : base(id, fullName, name)
        {
            
        }

        internal override void AddFamix(StringBuilder sb)
        {
            sb.AppendLine();
            sb.Append("\t(FAMIX.ParameterizableClass");
            AddId(sb);
            AddName(sb);
            AddNamespace(sb);
            sb.AppendLine();
            sb.Append("\t\t(isStub true)");
            AddModifiers(sb);
            sb.Append(')');
        }
    }
}
