using System;

namespace Famix
{
    public class ProgressEventArgs : EventArgs
    {
        public ProgressEventArgs(string message)
        {
            Message = message;
        }

        public string Message { get; private set; }
    }
}
