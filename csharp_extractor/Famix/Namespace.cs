using System.Text;

namespace Famix
{
    public class Namespace : FamixNameType
    {
        public Namespace(int id, string fullName, string name)
            :base(id, fullName, name)
        {
            
        }

        internal override void AddFamix(StringBuilder sb)
        {
            sb.AppendLine();
            sb.Append("\t(FAMIX.Namespace");
            AddId(sb);
            AddName(sb);
            //(isStub true)
            if (Parent != null)
            {
                sb.AppendLine();
                sb.Append("\t(parentScope (ref: " + Parent.Id + "))");
            }
            sb.Append(')');
        }

        public Namespace Parent { get; internal set; }
    }
}
