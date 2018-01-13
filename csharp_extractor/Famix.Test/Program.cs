using System;
using System.IO;
using System.Linq;

namespace Famix.Test
{
    class Program
    {
        static void Main(string[] args)
        {
            Console.WriteLine("Famix Extensions for Microsoft .NET-Framework");
            Console.WriteLine("Copyright (c) 2012 by Christian Mählig\n");
            
            //Type in path to the Assembly
            Console.WriteLine("Please type in the path to the Assembly: ");
            string path = Console.ReadLine();

            //string path = @"C:\Users\Ramon\Downloads\sharpPDF-1_2\sharpPDF-1_2\sharpPDF.dll";
            //string path = @"C:\Users\Ramon\Dropbox\_Studium\14_SS_2015\Softwaresystemfamilien und -produktlinien\Famix Prototype\Famix.dll";
            //string path = @"C:\Users\Ramon\Dropbox\_Studium\14_SS_2015\Softwaresystemfamilien und -produktlinien\Famix Prototype\Famix.Test.exe";
            //string path = @"C:\Users\Ramon\Dropbox\_Studium\14_SS_2015\Softwaresystemfamilien und -produktlinien\Famix Prototype\SampleLibrary\SampleLibrary\bin\Debug\SampleLibrary.dll";
            if (path.IndexOfAny(Path.GetInvalidPathChars()) != -1)
            {
                Console.WriteLine("\n>Please type in a valid path to your Assembly.");
            }
            else
            {
                string extension = Path.GetExtension(path);
                if (extension.EndsWith(".exe") || extension.EndsWith(".dll") || extension.EndsWith(".assembly"))
                {
                    try
                    {
                        Parser parser = new Parser();
                        parser.ProgressChanged += parser_ProgressChanged;

                        parser.ParseAssembly(path);

                        parser.Save(Path.GetFileNameWithoutExtension(path) + ".famix");
                    }
                    catch (Exception e)
                    {
                        Console.WriteLine("\n>Exception occured in " + e.Source);
                        Console.WriteLine("\n" + e.Message);
                    }
                }
                else
                {
                    Console.WriteLine("\n>The File \"" + Path.GetFileName(path) + "\" is not a valid .NET-Assembly.");
                }                
            }
            Console.ReadLine();
        }

        static void parser_ProgressChanged(object sender, ProgressEventArgs e)
        {
            Console.WriteLine(e.Message);
        }
    }
}
