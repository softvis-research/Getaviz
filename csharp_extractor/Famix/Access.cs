using System.Text;

namespace Famix
{
    public class Access : FamixType
    {
        public Access(int id)
            : base(id)
        {
            
        }

        internal override void AddFamix(StringBuilder sb)
        {
            sb.AppendLine();
            sb.Append("\t(FAMIX.Access");
            AddId(sb);
            sb.AppendLine();
            sb.Append("\t\t(accessor (ref: " + Accessor.Id + "))");
            if (IsWrite)
            {
                sb.AppendLine();
                sb.Append("\t\t(isWrite true)");
            }
            sb.AppendLine();
            sb.Append("\t\t(variable (ref: " + Variable.Id + "))");
            sb.Append(')');
        }

        public Method Accessor { get; internal set; }
        public Attribute Variable { get; internal set; }
        public bool IsWrite { get; internal set; }
    }
}
