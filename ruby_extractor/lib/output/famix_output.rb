module FamixOutput

  class DummyClass
    attr_accessor :namespace, :files
    @@written_ids = []
    def type
      return :class
    end
    def name
      return '__dummy__'
    end
    def path
      namespace.path.to_s + "::__dummy__"
    end

    def was_written?
      return true if @@written_ids.include?(self.path)
      return false
    end

    def written
      @@written_ids << self.path
    end

    def children
      return []
    end
    
  end
  

  def write_famix(output_file, version = 0)
    File.open(output_file, 'w+'){|file|
      file.puts "("
      version(version).objects.each{|o|
        file.puts to_famix(o)
      } 
      file.puts ")" 
    }
  end

  

  def to_famix(object)
    case (object.type)
    when :module
      if object.namespace.nil? or (object.namespace and not(object.namespace.type == :class))
        return to_famix_namespace(object)
      else
        return to_famix_class(object)
      end
    when :class
      return to_famix_class(object)
    when :classvariable
      return to_famix_classvariable(object)
    when :constant
      return to_famix_constant(object)
    when :method
      return to_famix_method(object)
    else
      puts "#{object.type} unbekannt"
    end
  end


  def to_famix_namespace(object)
    output = "(FAMIX.Namespace \n"
    id = RubyParser::Helper.get_id_for_object(object)
    output << "   ( id: #{id})\n" if id
    output << "   ( name '#{object.name.to_s.strip}')\n"
    if object.namespace and not(object.namespace.to_s.strip.empty?) 
      namespace_id = RubyParser::Helper.get_id_for_object(object.namespace)
      output << "   ( parentScope (ref: #{namespace_id}))\n" if namespace_id
    end
    
    output << ")"
    return output

  end


  def to_famix_class(object)
    id = RubyParser::Helper.get_id_for_object(object)

    
    
    output = "(FAMIX.Class \n"
    output << "   ( id: #{id})\n" if id
    output << "   ( name '#{object.name.to_s.strip}')\n"
    namespace_id = RubyParser::Helper.get_id_for_object(object.namespace)
    output << "   ( container (ref: #{namespace_id}))\n" if namespace_id
    output << "   ( isInterface false)\n"
    #  output << "   (fileName '#{RubyParser::Helper.get_relative_path(object.files)}')\n"
    # output << "   (startLine -1)\n"
    # output << "   (endLine -1)\n"
    # output << "   (GodClass false)"
    # output << "   (DataClass false)"
    # output << "   (BrainClass false)"
    # output << "   (RefusedParentBequest false)"
    # output << "   (TraditionBreaker false)"
    # output << "   (WLOC 0.0)"
    # output << "   (WNOS 0.0)"
    # output << "   (WNOCond 0.0)"
    # output << "   (WNOCmts 0.0)"
    # output << "   (WOC 0.0)"
    # output << "   (ATFD 0.0)"
    # output << "   (WMC 0.0)"
    # output << "   (TCC 0.0)"
    # output << "   (CRIX 0.0)"
    # output << "   (NOAM 0.0)"
    # output << "   (NOPA 0.0)"
    # output << "   (BUR 0.0)"
    # output << "   (BOvR 0.0)"
    # output << "   (AMW 0.0)"
    # output << "   (NOM 0.0)"
    # output << "   (NAS 0.0)"
    # output << "   (PNAS 0.0)"
    # output << "   (LOC 0.0)\n"
    # output << "   (NProtM 0.0)"
    output << ")\n"

    output << to_famix_file_anchor(object, id, RubyParser::Helper.get_relative_path(object.files), 1, 1)

    
    output << create_root(object.namespace) if namespace_id == 0

    
    
    
    if object.respond_to? :written
      if object.was_written?
        ### war schonmal da
        return ""
      else
        object.written
      end
      
      
    end
    
    
    return output

  end


  def create_root(object)
    output = "(FAMIX.Namespace \n"
    id = RubyParser::Helper.get_id_for_object(object)
    output << "   ( id: #{id})\n" if id
    output << "   ( name '#{object.name.to_s.strip}')\n"
    output << ")"
    return output  
  end

  def to_famix_method(object)
    
    id = RubyParser::Helper.get_id_for_object(object)
    loc = 0
    loc = object.source.lines.length if object.source
    if self.respond_to?(:metrics)
      method_metrics = metrics.read_metrics_for_yard_method(object)
    end
    
    namespace_id = RubyParser::Helper.get_id_for_object(object.namespace)
    output = ""
    if object.namespace.type == :module
      dummy = DummyClass.new
      dummy.namespace = object.namespace
      dummy.files = object.files
      namespace_id = RubyParser::Helper.get_id_for_object(dummy)
      output << to_famix_class(dummy)
      output << "\n\n"
    end
    
    
    output << "(FAMIX.Method\n"
    output << "	( id: #{id})\n"
    output << "	( name '#{object.name.to_s.strip}')\n"
    if self.respond_to?(:metrics)
      output << "	( cyclomaticComplexity #{method_metrics[:complexity]})\n"
    end
    output << "	( hasClassScope #{object.scope == :class ? "true": "false"})\n"
    #  output << "	( fileName '#{RubyParser::Helper.get_relative_path(object.files)}')\n"
    # output << "	( startLine #{object.line} )\n"
    # output << "	( endLine #{object.line + loc} )\n"
    output << "	( modifiers '#{object.visibility}')\n"

    
    
    
    output << "   ( parentType (ref: #{namespace_id}))\n" if namespace_id
    if object.signature
      output << "	( signature '#{object.signature.gsub("'",'"').gsub(/(\\)/, '\1\1')}')\n"
    else
      signature = object.name.to_s.strip
      output << "	( signature '#{signature.gsub("'",'"').gsub(/(\\)/, '\1\1')}')\n"
    end

    
    
    #raise "OO" unless 
    

    
    # output << "	( isAbstract false)\n"
    # output << "	( isConstructor false)\n"
    # output << "	( isPureAccessor false)\n"
    # output << "	( FeatureEnvy false)\n"
    # output << "	( BrainMethod false)\n"
    # output << "	( IntensiveCoupling false)\n"
    # output << "	( DispersedCoupling false)\n"
    # output << "	( ShotgunSurgery false)\n"
    # output << "	( NOS 0.0)\n"
    # output << "	( NOCond 0.0)\n"
    # output << "	( NMAA 0.0)\n"
    # output << "	( NI 0.0)\n"
    # output << "	( NOCmts 0.0)\n"
    
    # output << "	( CINT 0.0)\n"
    # output << "	( CDISP 0.0)\n"
    # output << "	( CM 0.0)\n"
    # output << "	( CC 0.0)\n" 
    # output << "	( ATFD 0.0)\n"
    # output << "	( LAA 0.0)\n"
    # output << "	( FDP 0.0)\n"
#    output << "	( LOC #{'%.2f' % loc})\n"
    # output << "	( MAXNESTING 0.0)\n"
    # output << "	( NOAV 0.0)\n"
    output << ")"

    
    
    output << to_famix_file_anchor(object, id, RubyParser::Helper.get_relative_path(object.files), (object.line ? object.line : 1), (object.line ? object.line : 1) + loc)
    return output
  end


  def to_famix_classvariable(object)
    
    id = RubyParser::Helper.get_id_for_object(object)
    loc = object.source.lines.length
    output = ""
    output << "(FAMIX.Attribute\n"
    output << "	( id: #{id})\n"
    output << "	( name '#{object.name}')\n"
    output << "	( hasClassScope true)\n"
    output << "	( modifiers '#{object.visibility}')\n"
    namespace_id = RubyParser::Helper.get_id_for_object(object.namespace)
    output << "   ( parentType (ref: #{namespace_id}))\n" if namespace_id

    #output << "	( NMAV 0.0)\n"
    output << ")"

    output << to_famix_file_anchor(object, id, RubyParser::Helper.get_relative_path(object.files), 1, 1)
    
    return output
  end



  def to_famix_constant(object)
    output = ""
    return output
    output << "(FAMIX.Constant\n"
    output << "	( id: #{id})\n"
    output << "	( name '#{object.name}')\n"
    output << "	( hasClassScope true)\n"
    output << "	( modifiers '#{object.visibility}')\n"
    namespace_id = RubyParser::Helper.get_id_for_object(object.namespace)
    output << "   ( parentType (ref: #{namespace_id}))\n" if namespace_id
    
  end


  def to_famix_file_anchor(object, source_id, filename, first_line = 1, last_line = 1)
    output = ""
    output << "\n(FAMIX.FileAnchor\n"
    output << "  ( id: #{RubyParser::Helper.get_id_for_fileanchor(filename, source_id)})\n"
    output << "	( element (ref: #{source_id}))\n"
    output << "	( endLine #{last_line})\n"
    output << "	( fileName '#{RubyParser::Helper.get_relative_path(object.files)}')\n"
    output << "	( startLine #{first_line})\n"
    output << ")\n"  

  end

  

end
