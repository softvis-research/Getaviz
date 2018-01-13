using System.Collections.Generic;
using System.Text;

namespace Famix
{
    public class Enum : TypeBase
    {
        internal Enum(int id, string fullName, string name)
            : base(id, fullName, name)
        {
            Values = new List<EnumValue>();
        }

        internal override void AddFamix(StringBuilder sb)
        {
            sb.AppendLine();
            sb.Append("\t(FAMIX.Enum");
            AddId(sb);
            AddName(sb);
            AddNamespace(sb);
            //('(' 'isStub' isStub=Boolean ')')?
            AddModifiers(sb);
            //('(' 'sourceAnchor' sourceAnchor=IntegerReference ')')?
            sb.Append(')');

            foreach (var enumValue in Values)
                enumValue.AddFamix(sb);
        }

        public List<EnumValue> Values { get; private set; }
    }
}
