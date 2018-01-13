using System.Text;

namespace Famix
{
    public class Anchor : FamixType
    {
        public Anchor(int id)
            : base(id)
        {

        }

        internal override void AddFamix(StringBuilder sb)
        {
            sb.AppendLine();
            sb.Append("\t(FAMIX.FileAnchor");
            AddId(sb);
            sb.AppendLine();
            sb.Append("\t\t(element (ref: " + Element.Id + "))");
            sb.AppendLine();
            sb.Append("\t\t(endLine " + EndLine + ")");
            sb.AppendLine();
            sb.Append("\t\t(fileName '" + FileName + "')");
            sb.AppendLine();
            sb.Append("\t\t(startLine " + StartLine + "))");
        }

        public int StartLine { get; internal set; }
        public int EndLine { get; internal set; }
        public string FileName { get; internal set; }

        public FamixType Element { get; internal set; }
    }
}
