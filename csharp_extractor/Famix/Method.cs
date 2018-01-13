using System.Collections.Generic;
using System.Linq;
using System.Text;
using Famix.BodyReader;

namespace Famix
{
    public class Method : FamixNameType
    {
        public Method(int id, string fullName, string name)
            : base(id, fullName, name)
        {
            Parameters = new List<Parameter>();
        }

        internal override void AddFamix(StringBuilder sb)
        {
            Anchor.AddFamix(sb);
            sb.AppendLine();
            sb.Append("\t(FAMIX.Method");
            AddId(sb);
            AddName(sb);
            //sb.AppendLine();
            //sb.Append("\t(cyclomaticComplexity 1)");
            //sb.AppendLine();
            //sb.Append("\t(isStub true)");

            sb.AppendLine();
            if (IsConstructor)
                sb.Append("\t\t(kind 'constructor')");
            else
                sb.Append("\t\t(declaredType (ref: " + Type.Id + "))");

            sb.AppendLine();
            sb.Append("\t\t(modifiers '" + GetModifier() + "')");
            sb.AppendLine();
            sb.Append("\t\t(numberOfStatements " + Parameters.Count + ")");
            sb.AppendLine();
            sb.Append("\t\t(parentType (ref: " + ParentType.Id + "))");

            string signature = Parameters.Aggregate(Name + "(", (current, p) => current + (p.Type.Name + " " + p.Name + ", "));

            if (Parameters.Count > 0)
                signature = signature.Remove(signature.Length - 2);

            signature += ")";

            sb.AppendLine();
            sb.Append("\t\t(signature '" + signature + "')");

            sb.AppendLine();
            sb.Append("\t\t(sourceAnchor (ref: " + Anchor.Id + "))");
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
        public TypeBase ParentType { get; internal set; }
        public TypeBase Type { get; internal set; }
        public bool IsConstructor { get; internal set; }
        public bool IsAbstract { get; internal set; }
        public List<Parameter> Parameters { get; private set; }

        internal MethodBodyReader BodyReader { get; set; }
    }
}
