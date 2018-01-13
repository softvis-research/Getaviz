using System;
using System.Collections.Generic;
using System.Reflection;
using System.Reflection.Emit;

namespace Famix.BodyReader
{
    internal class MethodBodyReader
    {
        /// <summary>
        /// MethodBodyReader constructor
        /// </summary>
        /// <param name="methodBase">The System.Reflection defined MethodInfo
        /// </param>
        public MethodBodyReader(Method method, MethodBase methodBase)
        {
            Method = method;
            MethodBase = methodBase;
            Instructions = new List<Instruction>();
        }

        #region il read methods
        private int ReadInt16(byte[] il, ref int position)
        {
            return ((il[position++] | (il[position++] << 8)));
        }
        private ushort ReadUInt16(byte[] il, ref int position)
        {
            return (ushort)((il[position++] | (il[position++] << 8)));
        }
        private int ReadInt32(byte[] il, ref int position)
        {
            return (((il[position++] | (il[position++] << 8)) | (il[position++] << 0x10)) | (il[position++] << 0x18));
        }
        private ulong ReadInt64(byte[] il, ref int position)
        {
            return (ulong)(((il[position++] | (il[position++] << 8)) | (il[position++] << 0x10)) | (il[position++] << 0x18) | (il[position++] << 0x20) | (il[position++] << 0x28) | (il[position++] << 0x30) | (il[position++] << 0x38));
        }
        private double ReadDouble(byte[] il, ref int position)
        {
            return (((il[position++] | (il[position++] << 8)) | (il[position++] << 0x10)) | (il[position++] << 0x18) | (il[position++] << 0x20) | (il[position++] << 0x28) | (il[position++] << 0x30) | (il[position++] << 0x38));
        }
        private sbyte ReadSByte(byte[] il, ref int position)
        {
            return (sbyte)il[position++];
        }
        private byte ReadByte(byte[] il, ref int position)
        {
            return (byte)il[position++];
        }
        private Single ReadSingle(byte[] il, ref int position)
        {
            return (Single)(((il[position++] | (il[position++] << 8)) | (il[position++] << 0x10)) | (il[position++] << 0x18));
        }
        #endregion

        /// <summary>
        /// generate the list of Instructions according to the IL byte code.
        /// </summary>
        internal void GenerateInstructions()
        {
            if (MethodBase.GetMethodBody() == null)
                return;

            byte[] il = MethodBase.GetMethodBody().GetILAsByteArray();
            Module module = MethodBase.Module;

            int position = 0;
            
            while (position < il.Length)
            {
                Instruction instruction = new Instruction(this);

                // get the operation code of the current instruction
                OpCode code = OpCodes.Nop;
                ushort value = il[position++];
                if (value != 0xfe)
                    code = ReaderHelper.singleByteOpCodes[value];
                else
                {
                    value = il[position++];
                    code = ReaderHelper.multiByteOpCodes[value];
                }

                instruction.Code = code;
                instruction.Offset = position - 1;
                int metadataToken = 0;
                // get the operand of the current operation
                switch (code.OperandType)
                {
                    case OperandType.InlineBrTarget:
                        metadataToken = ReadInt32(il, ref position);
                        metadataToken += position;
                        instruction.Operand = metadataToken;
                        break;
                    case OperandType.InlineField:
                        metadataToken = ReadInt32(il, ref position);
                        instruction.Operand = module.ResolveField(metadataToken);
                        break;
                    case OperandType.InlineMethod:
                        metadataToken = ReadInt32(il, ref position);
                        try
                        {
                            instruction.Operand = module.ResolveMethod(metadataToken);
                        }
                        catch
                        {
                            instruction.Operand = module.ResolveMember(metadataToken);
                        }
                        break;
                    case OperandType.InlineSig:
                        metadataToken = ReadInt32(il, ref position);
                        instruction.Operand = module.ResolveSignature(metadataToken);
                        break;
                    case OperandType.InlineTok:
                        metadataToken = ReadInt32(il, ref position);
                        try
                        {
                            instruction.Operand = module.ResolveType(metadataToken);
                        }
                        catch
                        {

                        }
                        // SSS : see what to do here
                        break;
                    case OperandType.InlineType:
                        metadataToken = ReadInt32(il, ref position);
                        // now we call the ResolveType always using the generic attributes type in order
                        // to support decompilation of generic methods and classes

                        // thanks to the guys from code project who commented on this missing feature

                        try
                        {
                            instruction.Operand = module.ResolveType(metadataToken,
                                MethodBase.DeclaringType.GetGenericArguments(), MethodBase.GetGenericArguments());
                        }
                        catch
                        {

                        }
                        // SSS : see what to do here, Static Constructors
                        break;
                    case OperandType.InlineI:
                    {
                        instruction.Operand = ReadInt32(il, ref position);
                        break;
                    }
                    case OperandType.InlineI8:
                    {
                        instruction.Operand = ReadInt64(il, ref position);
                        break;
                    }
                    case OperandType.InlineNone:
                    {
                        instruction.Operand = null;
                        break;
                    }
                    case OperandType.InlineR:
                    {
                        instruction.Operand = ReadDouble(il, ref position);
                        break;
                    }
                    case OperandType.InlineString:
                    {
                        metadataToken = ReadInt32(il, ref position);
                        instruction.Operand = module.ResolveString(metadataToken);
                        break;
                    }
                    case OperandType.InlineSwitch:
                    {
                        int count = ReadInt32(il, ref position);
                        int[] casesAddresses = new int[count];
                        for (int i = 0; i < count; i++)
                        {
                            casesAddresses[i] = ReadInt32(il, ref position);
                        }
                        int[] cases = new int[count];
                        for (int i = 0; i < count; i++)
                        {
                            cases[i] = position + casesAddresses[i];
                        }
                        break;
                    }
                    case OperandType.InlineVar:
                    {
                        instruction.Operand = ReadUInt16(il, ref position);
                        break;
                    }
                    case OperandType.ShortInlineBrTarget:
                    {
                        instruction.Operand = ReadSByte(il, ref position) + position;
                        break;
                    }
                    case OperandType.ShortInlineI:
                    {
                        instruction.Operand = ReadSByte(il, ref position);
                        break;
                    }
                    case OperandType.ShortInlineR:
                    {
                        instruction.Operand = ReadSingle(il, ref position);
                        break;
                    }
                    case OperandType.ShortInlineVar:
                    {
                        instruction.Operand = ReadByte(il, ref position);
                        break;
                    }
                    default:
                    {
                        throw new Exception("Unknown operand type.");
                    }
                }

                instruction.GenerateCode();

                Instructions.Add(instruction);
            }
        }

        public Method Method { get; private set; }
        public MethodBase MethodBase { get; private set; }
        public List<Instruction> Instructions { get; private set; }
    }
}
