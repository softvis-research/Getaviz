load File.expand_path(File.join(File.dirname(__FILE__), "output", "dynamix_output.rb"))
class RubyParser::BehaviourParser < RubyParser::StructureParser

  include DynamixOutput

  RubyParser::Version.send(:define_method, "trace=") do |trace|
    @trace = trace
  end

  RubyParser::Version.send(:define_method, "trace") do 
    return @trace
  end
 
  
  def initialize(root, parser_type = :ruby19, only_parse_app_dir = false)
    RubyParser::Helper.root = root
    @only_parse_app_dir = only_parse_app_dir
    YARD::Parser::SourceParser.parser_type = parser_type
    recode_files
    puts "Parse Files"
    YARD::Registry.load(files_to_parse, true)
    puts "Done"    
    @famix_counter = 1
    @famix_ids = {}
    @versions = []
    self.new_version!
  end


  def parse_script_run!(script_name, args)
    trace = []
    old_args = ARGV
    ARGV.clear
    args.each{|arg|
      ARGV.push(arg)
    }


    Kernel.set_trace_func proc {
      |event, file, line, id, binding, classname| 
      if (event == "call" or event == "return" ) and (file.include?(RubyParser::Helper.root))

        trace << {:event => event, :time => Time.now.to_f, :id => id.to_s, :line => line, :class_name => classname.to_s, :file => file}
      end
    }
    load script_name
    Kernel.set_trace_func nil
    ARGV.clear
    old_args.each{|old_arg|
      ARGV.push(old_arg)
    }

    @versions.last.trace = trace
    
  end

 

end
