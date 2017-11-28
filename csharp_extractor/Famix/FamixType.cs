using System.Text;

namespace Famix
{
    public abstract class FamixType
    {
        protected FamixType(int id)
        {
            Id = id;
        }

        internal abstract void AddFamix(StringBuilder sb);

        internal void AddId(StringBuilder sb)
        {
            sb.Append(" (id: " + Id + ")");
        }

        public int Id { get; private set; }
    }
}
