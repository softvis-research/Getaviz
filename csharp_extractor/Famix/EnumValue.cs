using System.Text;

namespace Famix
{
    public class EnumValue : FamixNameType
    {
        internal EnumValue(int id, string fullName, string name)
            : base(id, fullName, name)
        {
            
        }

        internal override void AddFamix(StringBuilder sb)
        {
            sb.AppendLine();
            sb.Append("\t(FAMIX.EnumValue");
            AddId(sb);
            AddName(sb);
            //sb.AppendLine();
            //sb.Append("\t(isStub true))");
            sb.AppendLine();
            sb.Append("\t\t(parentEnum (ref: " + ParentEnum.Id + "))");
            sb.Append(')');
        }

        public Enum ParentEnum { get; internal set; }
    }
}
