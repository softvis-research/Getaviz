require 'git'
require 'tmpdir'

load File.expand_path(File.join(File.dirname(__FILE__), "output", "hismo_output.rb"))

class RubyParser::EvolutionParser < RubyParser::StructureParser
  include HismoOutput

  def self.parse_diff_file(diff_file, old_version, new_version)
    
    patch = diff_file.patch
    puts diff_file.path
    changed_lines_in = []
    changed_lines_out = []
    offset_line_in = 0
    offset_line_out = 0
    patch.each_line{|line|
      if line =~ /^@@[[:space:]]([[:digit:]]+),([[:digit:]]+)[[:punct:][:space:]]+([[:digit:]]+),([[:digit:]]+)/
        offset_line_in = $1.to_i
        offset_line_out = $3.to_i
      end
      if line =~ /^\+/
        offset_line_in -= 1
        changed_lines_out << offset_line_out
      end
      if line =~ /^-/
        offset_line_out -= 1
        changed_lines_in << offset_line_in
      end
      offset_line_in += 1
      offset_line_out += 1
    }

    changed_classes = []
    
    [old_version,new_version].each{|version| version.objects.each{|the_class|
                                     next unless the_class.type.to_sym == :class
                                     if changed_lines_in.find{|line| (the_class.begin_line..the_class.begin_line).include?(line)}
                                       changed_classes << the_class.path
                                       break
                                     end
                                   }
    }
    # puts changed_classes.to_yaml
    
    
  end

  
  def initialize(repo, parser_type = :ruby19, only_parse_app_dir = false)
    @repo = repo
    @only_parse_app_dir = only_parse_app_dir
    @famix_counter = 1
    @famix_ids = {}
    @versions = []
    @commits = []
    @all_objects = []
    
    @namespace_histories = {}
    @class_histories = {}
    @method_histories = {}
    @attribute_histories = {}
    @constant_histories = {}
    
    @namespace_versions = {}
    @class_versions = {}
    @method_versions = {}
    @attribute_versions = {}
    @constant_versions = {}
    
    @version_counter = 0
    @class_dummies = {}
    
    YARD::Parser::SourceParser.parser_type = parser_type
  end

  def find_or_create_namespace_version(object, commit)
    #### Namespaces
    ####
    version_path = "Version#{@version_counter}____#{object.path}"
    return @namespace_versions[version_path] if @namespace_versions[version_path]
    namespace_history = find_or_create_namespace_history(object)
    namespace_version = HismoNamespaceVersion.new
    namespace_version.id = RubyParser::Helper.new_counter
    namespace_version.name = object.name
    namespace_version.commit_id =  commit.objectish
    namespace_version.timestamp = commit.committer.date.strftime("%FT%R")
    date = commit.committer.date
    namespace_version.yard_object = object
    @namespace_versions[version_path] = namespace_version
    @namespace_histories[object.path].namespace_versions << namespace_version
    namespace_version.parent_history = namespace_history.id
    @all_objects << @namespace_versions[version_path]
    return namespace_version
  end

  def find_or_create_class_version(object, commit)
    version_path = "Version#{@version_counter}____#{object.path}"
    return  @class_versions[version_path] if @class_versions[version_path]
    class_history = find_or_create_class_history(object)
    class_version = HismoClassVersion.new
    class_version.id = RubyParser::Helper.new_counter
    class_version.name = object.name
    class_version.commit_id =  commit.objectish
    class_version.timestamp = commit.committer.date.strftime("%FT%R")
    class_version.yard_object = object
    @class_histories[object.path].class_versions << class_version
    class_version.parent_history = class_history.id
    @class_versions[version_path] = class_version
    @all_objects << class_version
    return class_version
  end
  
  def find_or_create_method_version(object, commit)
    ###
    ### Methods
    ### :id, :name, :containing_class_history, :method_versions
    unless object.namespace.type == :root or (object.namespace and object.namespace.namespace and object.namespace.namespace.type == :proxy)
      if YARD::Registry.all.include?(object.namespace)
        version_path = "Version#{@version_counter}____#{object.path}"
        return @method_versions[version_path] if @method_versions[version_path]
        method_history = find_or_create_method_history(object)
        method_version = HismoMethodVersion.new
        method_version.id = RubyParser::Helper.new_counter
        method_version.name = object.name
        method_version.commit_id =  commit.objectish
        method_version.timestamp = commit.committer.date.strftime("%FT%R")
        loc = object.source.lines.length
        method_version.loc = loc
        method_history.min_loc = loc if method_history.min_loc.nil? or method_history.min_loc > loc
        method_history.max_loc = loc if method_history.max_loc.nil? or method_history.max_loc < loc
        method_version.yard_object = object
        method_history.method_versions << method_version
        method_version.parent_history = method_history.id
        @method_versions[version_path] = method_version
        @all_objects << method_version
        return method_version
      end
    end
    return nil
  end


  def find_or_create_classvariable_version(object, commit)
    version_path = "Version#{@version_counter}____#{object.path}"
    return @attribute_versions[version_path] if @attribute_versions[version_path]
    attribute_history = find_or_create_classvariable_history(object)
    attribute_version = HismoAttributeVersion.new
    attribute_version.id = RubyParser::Helper.new_counter
    attribute_version.name = object.name
    attribute_version.commit_id =  commit.objectish
    attribute_version.timestamp = commit.committer.date.strftime("%FT%R")
    attribute_version.yard_object = object
    @attribute_versions[version_path] = attribute_version
    attribute_history.attribute_versions << attribute_version
    attribute_version.parent_history = attribute_history.id
    @all_objects << attribute_version
    return attribute_version
  end

  def find_or_create_constant_version(object, commit)
    return nil if object.namespace.type == :root
    version_path = "Version#{@version_counter}____#{object.path}"
    return @constant_versions[version_path] if @constant_versions[version_path]
    constant_history = find_or_create_constant_history(object.namespace)
    constant_version = HismoConstantVersion.new
    constant_version.id = RubyParser::Helper.new_counter
    constant_version.name = object.name
    constant_version.commit_id =  commit.objectish
    constant_version.timestamp = commit.committer.date.strftime("%FT%R")
    constant_version.yard_object = object
    @constant_versions[version_path] = constant_version
    constant_history.constant_versions << constant_version
    constant_version.parent_history = constant_history.id
    @all_objects << constant_version
    return constant_version
  end

  def find_or_create_namespace_history(object)
    object_path = ""
    object_path = object.path if object
    return @namespace_histories[object_path] if @namespace_histories[object_path]
    history = HismoNamespaceHistory.new
    history.id = RubyParser::Helper.new_counter
    history.namespace_versions = []
    history.namespace_histories = []
    history.class_histories = []
    @namespace_histories[object.path] = history
    @all_objects << history
    if object and object.namespace
      parent_namespace_history = find_or_create_namespace_history(object.namespace)
      unless parent_namespace_history.namespace_histories.include?(history)
        parent_namespace_history.namespace_histories << history
        @namespace_histories[object.namespace.path] = parent_namespace_history
      end
    end
    return @namespace_histories[object.path]
  end

  def find_or_create_dummy_class_history(object)
    return @class_histories[object.namespace.path + '::Dummy'] if @class_histories[object.namespace.path + '::Dummy']
    class_history = HismoClassHistory.new
    class_history.id = RubyParser::Helper.new_counter
    class_history.name = 'Dummy'
    class_history.class_versions = []
    class_history.method_histories = []
    class_history.attribute_histories = []
    class_history.constant_histories = []
    class_history.containing_namespace_history = find_or_create_namespace_history(object.namespace)
    class_history.containing_namespace_history.class_histories << class_history
    @class_histories[object.namespace.path + '::Dummy'] = class_history
    @all_objects << @class_histories[object.namespace.path + '::Dummy']
    return class_history
  end

  def find_or_create_class_history(object)
#    return find_or_create_dummy_class_history(object) if object.type == :module
    return @class_histories[object.path] if @class_histories[object.path]    
    namespace_history = find_or_create_namespace_history(object.namespace)
    history = HismoClassHistory.new
    history.id = RubyParser::Helper.new_counter

    # if history.id == 170
    #   puts object.to_yaml
    #   raise
      
    # end
    
    history.name = object.name
    history.class_versions = []
    history.method_histories = []
    history.attribute_histories = []
    history.constant_histories = []
    history.containing_namespace_history = namespace_history
    history.containing_namespace_history.class_histories << history
    @class_histories[object.path] = history
    @all_objects << @class_histories[object.path]
    return history
  end

  def find_or_create_method_history(object)
    return @method_histories[object.path] if @method_histories[object.path]
    class_history = find_or_create_class_history(object.namespace)
    history = HismoMethodHistory.new
    history.id = RubyParser::Helper.new_counter
    history.name = object.name
    history.method_versions = []
    history.containing_class_history = class_history
    class_history.method_histories << history             
    @method_histories[object.path] = history
    @all_objects << history
    return history
  end


  def find_or_create_classvariable_history(object)
    return @attribute_histories[object.path] if @attribute_histories[object.path]
    class_history = find_or_create_class_history(object.namespace)
    history = HismoAttributeHistory.new
    history.id = RubyParser::Helper.new_counter
    history.name = object.name
    history.attribute_versions = []
    history.containing_class_history = class_history
    class_history.attribute_histories << history
    @attribute_histories[object.path] = history
    @all_objects << @attribute_histories[object.path]
    return history
  end

  def find_or_create_constant_history(object)
    
    
    return @constant_histories[object.path] if @constant_histories[object.path]
    class_history = find_or_create_class_history(object.namespace)
    history = HismoConstantHistory.new
    history.id = RubyParser::Helper.new_counter
    history.name = object.name
    history.constant_versions = []
    history.containing_class_history = class_history
    class_history.constant_histories << history
    @constant_histories[object.path] = history
    @all_objects << history
    return history
  end
  


  






  

  def parse!
    Dir.mktmpdir{|directory|
      RubyParser::Helper.root = nil
      if File.directory?(@repo)
        RubyParser::Helper.root = "#{directory}/#{File.basename(directory)}"
        FileUtils.cp_r(@repo, RubyParser::Helper.root)
        git = Git.open(RubyParser::Helper.root)#  , :log => Logger.new(STDOUT)
      else
        RubyParser::Helper.root = "#{directory}/repo"
        git = Git.clone(@repo, RubyParser::Helper.root)
      end
      
      git.checkout
      commits = git.log(100000)
      commit_array = []
      commits.each{|commit|
        commit_array << commit
      }
      commit_array.reverse!
      puts "Parsing #{commit_array.length} commits"

      commit_array.each_with_index{|commit,index|
        next unless index % 100 == 0        
        puts "Parse Commit Index #{index}"
        ### only each 10th commit
        
        @version_counter += 1
        
        git.reset_hard(commit)
        recode_files

        #        puts files_to_parse.to_yaml
        YARD::Registry.clear
        YARD::Registry.load(files_to_parse, true)
        @metrics = RubyParser::Metrics.new
        @metrics.generate_metrics(RubyParser::Helper.root, files_to_parse)
        # git.diff(commit, @commits.last).each{|diff_file|
        #   RubyParser::EvolutionParser.parse_diff_file(diff_file, @versions[-2], @versions.last) if File.extname(diff_file.path) == '.rb'
        # } if @commits.last
        version = new_version!
        version.objects.each{|object|
          # puts [:root,:module,:class,:constant,:method,:classvariable].index(object.type)
          # puts object.type
          # puts object.name
          
          case (object.type)
          when :module
            find_or_create_namespace_version(object, commit)
          when :class
            find_or_create_class_version(object, commit)
          when :classvariable
#            find_or_create_classvariable_version(object, commit)
          when :constant
#            find_or_create_constant_version(object, commit)
          when :method
            if object.namespace and object.namespace.type == :module
              dummy = DummyClass.new
              dummy.namespace = object.namespace
              dummy.files = object.files
              #find_or_create_class_version(dummy, commit)
              object.namespace = dummy
            end
            find_or_create_method_version(object, commit)
          else
            puts "#{object.type} unbekannt"
          end
        } 
        @commits << {:commit => commit, :version => version}
      }
      
    }
  end
  
end
