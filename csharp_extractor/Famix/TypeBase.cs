using System.Text;

namespace Famix
{
    public abstract class TypeBase : FamixNameType
    {
        public TypeBase(int id, string fullName, string name)
            : base(id, fullName, name)
        {
            
        }

        internal void AddNamespace(StringBuilder sb)
        {
            if (Namespace != null)
            {
                sb.AppendLine();
                sb.Append("\t\t(container (ref: " + Namespace.Id + "))");
            }
        }

        public TypeBase BaseType { get; internal set; }
        public Namespace Namespace { get; internal set; }
    }
}
