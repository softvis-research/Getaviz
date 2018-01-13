require 'yard'
require 'yard-activerecord'
require 'yaml'
require 'pathname'
require 'tmpdir'




module RubyParser

  class Helper

    @@root = nil

    def self.root
      return @@root
    end

    def self.root=(root)
      puts "set_root"
      @@root = root
    end
    
    def self.get_relative_path(path)
      return "" if path.nil? or path.first.nil? or path.first.first.nil?
      root = Pathname.new(@@root)
      pathname = Pathname.new(path.first.first)
      return pathname.relative_path_from(root) 
    end


    def self.get_id_for_object(object)
      return 0 if object.type == :root
      return 0 if object.type == :proxy
      @@ids = {} unless defined? @@ids 
      return @@ids[object.path] if @@ids[object.path]
      @@ids[object.path] = self.new_counter
      return @@ids[object.path]
    end

    def self.get_id_for_fileanchor(filename, sourceid)
      path = "#{filename}+#+#{sourceid}"
      return @@ids[path] if @@ids[path]
      @@ids[path] = self.new_counter
      return @@ids[path]
    end


    def self.new_counter
      @@counter = 1 unless defined? @@counter
      rv = @@counter
      @@counter += 1
      return rv
    end
    
  end

  
  
  
  class Version
    attr_accessor :author, :date, :yardoc_file
    def initialize()
      self.yardoc_file = YARD::Registry::DEFAULT_YARDOC_FILE + Time.now.to_i.to_s + rand(10000).to_s
      YARD::Registry.save(false, self.yardoc_file)
      YARD::Registry.clear
      YARD::Registry.load_yardoc(self.yardoc_file)
      @objects = YARD::Registry.all
    end
    
    def objects
      YARD::Registry.clear
      YARD::Registry.load_yardoc(self.yardoc_file)
      return @objects
    end
  end
  
  load File.expand_path(File.join(File.dirname(__FILE__), "structure_parser.rb"))
  load File.expand_path(File.join(File.dirname(__FILE__), "evolution_parser.rb"))
  load File.expand_path(File.join(File.dirname(__FILE__), "behaviour_parser.rb"))
  
end





