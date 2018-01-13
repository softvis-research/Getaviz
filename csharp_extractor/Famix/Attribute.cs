using System.Text;

namespace Famix
{
    public class Attribute : FamixNameType
    {
        public Attribute(int id, string fullName, string name)
            : base(id, fullName, name)
        {
        }

        internal override void AddFamix(StringBuilder sb)
        {
            sb.AppendLine();
            sb.Append("\t(FAMIX.Attribute");
            AddId(sb);
            AddName(sb);
            sb.AppendLine();
            sb.Append("\t\t(declaredType (ref: " + Type.Id + "))");
            sb.AppendLine();
            sb.Append("\t\t(modifiers '" + GetModifier() + "')");
            sb.AppendLine();
            sb.Append("\t\t(parentType (ref: " + ParentType.Id + "))");
            //(sourceAnchor (ref: 148)))
            sb.Append(')');
        }

        public TypeBase ParentType { get; internal set; }
        public TypeBase Type { get; internal set; }
        public bool IsInherited { get; internal set; }
    }
}
