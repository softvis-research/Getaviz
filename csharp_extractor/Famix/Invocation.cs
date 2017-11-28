using System.Text;

namespace Famix
{
    public class Invocation : FamixType
    {
        public Invocation(int id)
            : base(id)
        {

        }

        internal override void AddFamix(StringBuilder sb)
        {
            sb.AppendLine();
            sb.Append("\t(FAMIX.Invocation");
            AddId(sb);
            sb.AppendLine();
            sb.Append("\t\t(candidates (ref: " + Candidates.Id + "))");
            sb.AppendLine();
            sb.Append("\t\t(sender (ref: " + Sender.Id + "))");
            sb.AppendLine();
            sb.Append("\t\t(signature 'LinkedList()')");
            sb.Append(')');
        }

        public Method Sender { get; internal set; }
        public Method Candidates { get; internal set; }
    }
}
