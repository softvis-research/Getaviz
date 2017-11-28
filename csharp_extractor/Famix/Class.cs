using System.Collections.Generic;
using System.Text;

namespace Famix
{
    public class Class : TypeBase
    {
        public Class(int id, string fullName, string name)
            : base(id, fullName, name)
        {
            IsInterface = false;
            IsAbstract = false;

            Attributes = new List<Attribute>();
            Methods = new List<Method>();
        }

        internal override void AddFamix(StringBuilder sb)
        {
            if(Anchor != null)
                Anchor.AddFamix(sb);

            sb.AppendLine();
            sb.Append("\t(FAMIX.Class");
            AddId(sb);
            AddName(sb);
            AddNamespace(sb);
            if (IsInterface)
            {
                sb.AppendLine();
                sb.Append("\t\t(isInterface true)");
            }
            AddModifiers(sb);
            //(sourceAnchor (ref: 213))
            sb.Append(')');
        }

        internal override string GetModifier()
        {
             string modifier = base.GetModifier();

             if (IsAbstract)
                 modifier = "Abstract " + modifier;

            return modifier;
        }

        public Anchor Anchor { get; internal set; }
        public bool IsAbstract { get; internal set; }
        public bool IsInterface { get; internal set; }

        public List<Attribute> Attributes { get; private set; }
        public List<Method> Methods { get; private set; }
    }
}
