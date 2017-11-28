using System;
using System.Reflection;
using System.Reflection.Emit;

namespace Famix.BodyReader
{
    internal class Instruction
    {
        internal Instruction(MethodBodyReader bodyReader)
        {
            BodyReader = bodyReader;
        }

        // Fields
        private OpCode code;
        private object operand;
        private byte[] operandData;
        private int _offset;

        // Properties
        public OpCode Code
        {
            get { return code; }
            set { code = value; }
        }

        public object Operand
        {
            get { return operand; }
            set { operand = value; }
        }

        public byte[] OperandData
        {
            get { return operandData; }
            set { operandData = value; }
        }

        public int Offset
        {
            get { return _offset; }
            set { _offset = value; }
        }

        internal string SourceCode { get; private set; }

        internal bool ContainsAttribute
        {
            get
            {
                return SourceCode.Contains("stfld") || SourceCode.Contains("stsfld") || SourceCode.Contains("ldfld") ||
                       SourceCode.Contains("ldsfld");
            }
        }

        internal bool ContainsInvocation
        {
            get { return SourceCode.Contains(": call"); }
            // || SourceCode.Contains(": callvirt") || SourceCode.Contains(": newobj"); }
        }

        /// <summary>
        /// Returns a friendly string representation of this instruction
        /// </summary>
        /// <returns></returns>
        internal string GenerateCode()
        {
            string result = "";
            result += GetExpandedOffset(_offset) + " : " + Code;
            if (operand != null)
            {
                switch (Code.OperandType)
                {
                    case OperandType.InlineField:
                        FieldInfo fOperand = ((FieldInfo)operand);
                        result += " " + ReaderHelper.ProcessSpecialTypes(fOperand.FieldType.ToString()) + " " +
                            ReaderHelper.ProcessSpecialTypes(fOperand.ReflectedType.ToString()) +
                            "::" + fOperand.Name + "";
                        break;
                    case OperandType.InlineMethod:
                        try
                        {
                            MethodInfo mOperand = (MethodInfo)operand;
                            result += " ";
                            if (!mOperand.IsStatic) result += "instance ";
                            result += ReaderHelper.ProcessSpecialTypes(mOperand.ReturnType.ToString()) +
                                " " + ReaderHelper.ProcessSpecialTypes(mOperand.ReflectedType.ToString()) +
                                "::" + mOperand.Name + "()";
                        }
                        catch
                        {
                            try
                            {
                                ConstructorInfo mOperand = (ConstructorInfo)operand;
                                result += " ";
                                if (!mOperand.IsStatic) result += "instance ";
                                result += "void " +
                                    ReaderHelper.ProcessSpecialTypes(mOperand.ReflectedType.ToString()) +
                                    "::" + mOperand.Name + "()";
                            }
                            catch
                            {
                            }
                        }
                        break;
                    case OperandType.ShortInlineBrTarget:
                    case OperandType.InlineBrTarget:
                        result += " " + GetExpandedOffset((int)operand);
                        break;
                    case OperandType.InlineType:
                        result += " " + ReaderHelper.ProcessSpecialTypes(operand.ToString());
                        break;
                    case OperandType.InlineString:
                        if (operand.ToString() == "\r\n") result += " \"\\r\\n\"";
                        else result += " \"" + operand.ToString() + "\"";
                        break;
                    case OperandType.ShortInlineVar:
                        result += operand.ToString();
                        break;
                    case OperandType.InlineI:
                    case OperandType.InlineI8:
                    case OperandType.InlineR:
                    case OperandType.ShortInlineI:
                    case OperandType.ShortInlineR:
                        result += operand.ToString();
                        break;
                    case OperandType.InlineTok:
                        if (operand is Type)
                            result += ((Type)operand).FullName;
                        else
                            result += "not supported";
                        break;

                    default: result += "not supported"; break;
                }
            }

            SourceCode = result;

            return result;
        }

        /// <summary>
        /// Add enough zeros to a number as to be represented on 4 characters
        /// </summary>
        /// <param name="offset">
        /// The number that must be represented on 4 characters
        /// </param>
        /// <returns>
        /// </returns>
        private string GetExpandedOffset(long offset)
        {
            string result = offset.ToString();
            for (int i = 0; result.Length < 4; i++)
            {
                result = "0" + result;
            }
            return result;
        }

        internal MethodBodyReader BodyReader { get; private set; }
    }
}
