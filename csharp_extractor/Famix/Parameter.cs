namespace Famix
{
    public class Parameter
    {
        public Parameter(string name)
        {
            Name = name;
        }

        public string Name { get; private set; }
        public TypeBase Type { get; internal set; }
    }
}
