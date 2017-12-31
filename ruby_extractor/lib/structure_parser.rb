load File.expand_path(File.join(File.dirname(__FILE__), "output", "famix_output.rb"))

class YardClassHandler < YARD::Handlers::Ruby::ClassHandler

  handles :class
  
  def process
    name = self.statement.class_name.jump(:tstring_content, :ident).source
    object = YARD::CodeObjects::ClassObject.new(namespace, name)
    object['begin_line'] = self.statement.line_range.begin
    object['end_line'] = self.statement.line_range.end
  end

end




class RubyParser::Metrics

  def initialize
    @metrics = {}
  end

  def generate_metrics(root, files)
    Dir.mktmpdir{|dir|
      `saikuro --formater text -c -y 0 -i #{root}  -o #{dir}`
      Dir.glob("#{dir}/**/*.*").each{|file|
        
        array = []
        File.read(file).each_line{|line|
          next unless line =~ /^Type:Def Name:(.+) Complexity:(.+) Lines:(.+)/
          array << {:method_name => $1, :complexity => $2, :loc => $3}
        }
        @metrics[((Pathname.new("/") + Pathname.new(file).relative_path_from(Pathname.new(dir))).relative_path_from(Pathname.new(root))).to_s.gsub(/_cyclo\.html$/,'')] = array
      }
    }
    
  end

  
  def read_metrics_for_yard_method(yard_method)
    rv = nil
    rv =  @metrics[RubyParser::Helper.get_relative_path(yard_method.files).to_s].find{|m|
      m[:method_name] == (yard_method.scope == :class ? "self.#{yard_method.name}": yard_method.name.to_s) or
        m[:method_name] == (yard_method.scope == :class ? "#{yard_method.namespace.name}.#{yard_method.name}": yard_method.name.to_s)
    } if yard_method and yard_method.files and not(yard_method.files.empty?)

    unless rv
      return {:method_name => yard_method.name.to_s, :complexity => 1, :loc => 1}
    else
      return rv
    end

  end


end





class RubyParser::StructureParser

  include FamixOutput

  
  def initialize(root, parser_type = :ruby19, only_parse_app_dir = false)
    RubyParser::Helper.root = root
    @only_parse_app_dir = only_parse_app_dir
    YARD::Parser::SourceParser.parser_type = parser_type
    recode_files
    puts "Parse Files"
    YARD::Registry.load(files_to_parse, true)
    @metrics = RubyParser::Metrics.new
    @metrics.generate_metrics(root, files_to_parse)
    

    
    puts "Done"
    
    @@famix_ids = {}
    @versions = []
    self.new_version!
  end

  

  def new_version!(author = nil, date = nil)
    version = RubyParser::Version.new()
    version.author = author
    version.date = date
    @versions.push(version)
    return version
  end

  def version(number)
    return @versions[number]
  end

  def versions
    return @versions
  end

  def metrics
    return @metrics
  end

    
  
  

  def files_to_parse
    if (File.directory?(RubyParser::Helper.root))
      if @only_parse_app_dir
        files = Dir.glob("#{RubyParser::Helper.root}/app/**/*.rb")
      else
        files = Dir.glob("#{RubyParser::Helper.root}/lib/**/*.rb")
      end
    else
      files = ["#{RubyParser::Helper.root}"]
    end
  end

  def recode_files
    ### recode all files to utf-8
    files_to_parse.each{|file|
      string = File.read(file)
      File.open(file, 'w+'){|file|
        string.encode!("UTF-16", "UTF-8", {:invalid => :replace, :undef => :replace, :replace => ''})
        string.encode!('UTF-8', 'UTF-16')
        file.puts string
      }
    }
  end


end
