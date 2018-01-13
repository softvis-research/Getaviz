using System.Text;

namespace Famix
{
    public abstract class FamixNameType : FamixType
    {
        protected FamixNameType(int id, string fullName, string name)
            :base(id)
        {
            FullName = fullName;
            Name = name;
        }

        internal void AddName(StringBuilder sb)
        {
            sb.AppendLine();
            sb.Append("\t\t(name '" + Name + "')");
        }

        internal void AddModifiers(StringBuilder sb)
        {
            sb.AppendLine();
            sb.Append("\t\t(modifiers '" + GetModifier() + "')");
        }

        internal virtual string GetModifier()
        {
            if (IsPublic)
                return "Public";

            if (IsInternal)
                return "Internal";

            if (IsProtected)
                return "Protected";

            if (IsPrivate)
                return "Private";

            return string.Empty;
        }

        public string Name { get; private set; }
        public string FullName { get; private set; }

        public bool IsPrivate { get; internal set; }
        public bool IsProtected { get; internal set; }
        public bool IsInternal { get; internal set; }
        public bool IsPublic { get; internal set; }
    }
}
