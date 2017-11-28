using System.Text;

namespace Famix
{
    public class PrimitiveType : TypeBase
    {
        public PrimitiveType(int id, string fullName, string name)
            : base(id, fullName, name)
        { }

        internal override void AddFamix(StringBuilder sb)
        {
            sb.AppendLine();
            sb.Append("\t(FAMIX.PrimitiveType");
            AddId(sb);
            AddName(sb);
            sb.AppendLine();
            sb.Append("\t\t(isStub true))");
        }
    }
}
