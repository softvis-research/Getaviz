### UNFERTIG
### UNFERTIG
### UNFERTIG

module DynamixOutput
  class DynamixTrace
    attr_accessor :id, :name, :activations
    def to_s
      string = "(DynaMoose.Trace \n"
      string << "(id: #{self.id})\n"
      string << "(name '#{self.name.strip}')\n"
#      string << "(activations #{self.activations.collect{|a| "( ref: " + a.id.to_s + " )"}.join(" ")} ) " if self.activations
      # ARGS
      string << ")"
      return string
    end
  end

  class DynamixClass
    attr_accessor :id, :name, :methods, :instances
    def to_s
      string = "(FAMIX.Class \n"
      string << "(id: #{self.id})\n"
      string << "(name '#{self.name.strip}')\n"
#      string << "(methods #{self.methods.collect{|a| "( ref: " + a.id.to_s + " )"}.join(" ")}) " if self.methods
#     string << "(instances #{self.instances.collect{|a| "( ref: " + a.id.to_s + " )"}.join(" ")}) " if self.instances
      string << ")"
      return string
    end
  end

  class DynamixMethod
    attr_accessor :id, :name, :parent_type, :signature, :activations
    def to_s
      string = "(FAMIX.Method \n"
      string << "(id: #{self.id})\n"
      tmp_name = self.signature.strip.gsub(/def[[:space:]]+/,'').gsub(/(?<=\()(.*)(?=\))/){|match|
        match.split(",").collect{|param| param.gsub(/=[^,\)]+/,'')}.join(", ")
      }
      string << "(name '#{tmp_name.strip}')\n"
      string << "(belongsTo (idref: #{self.parent_type.id}))\n"# if self.parent_type
      string << "(signature '#{self.signature}') "
#     string << "(activations #{self.activations.collect{|a| "( ref: " + a.id.to_s + " )"}.join(" ")}) " if self.activations
      string << ")"
      return string
    end
  end

  class DynamixInstance
    attr_accessor :id, :type
    def to_s
      string = "(DynaMoose.Instance \n"
      string << "(id: #{self.id}) "
      string << "(instanceOf ( idref: #{self.type.id} ) ) " if self.type
      string << ")"
      return string
    end
  end

  class DynamixActivation
    attr_accessor :id, :start_time, :stop_time, :parent, :method, :activations, :trace
    def to_s
      string = "(DynaMoose.Activation \n"
      string << "(id: #{self.id})\n"
      string << "(trace (idref: #{self.trace.id}) )\n"
      string << "(method (idref: #{self.method.id}) )\n" #if self.method
      string << "(parent (idref: #{self.parent.id}) )\n" if self.parent
#      string << "(activations #{self.activations.collect{|a| "( ref: " + a.id.to_s + " )"}.join(" ")}) " if self.activations
      string << "(start #{self.start_time})\n"
      string << "(stop #{self.stop_time})\n"
      string << ")"
      return string
    end
  end

  
  def enriched_trace(trace)
    
    @objects_hash = {}
    @activation_stack = []

    dt = DynamixTrace.new
    dt.id = RubyParser::Helper.new_counter
    dt.name = 'main'

    @objects_hash[nil] = dt
    ##dynamix_trace << dt
    trace.each{|trace_entry|
      if trace_entry[:event] == 'call'
        class_fqn = trace_entry[:class_name]
        
        yard_namespace = YARD::Registry.all(:module).find{|tmp_module| tmp_module.path == class_fqn}
        if yard_namespace
          class_fqn += "::__dummy__"
        end
          

        
        method_fqn = "#{trace_entry[:class_name]}.#{trace_entry[:id]}"
        unless @objects_hash[class_fqn]
          tmpclass = DynamixClass.new
          
          tmpclass.id = RubyParser::Helper.new_counter
          tmpclass.name = class_fqn
          @objects_hash[class_fqn] = tmpclass
        end

        unless @objects_hash[method_fqn]
          tmpmethod = DynamixMethod.new
          
          tmpmethod.id = RubyParser::Helper.new_counter
          tmpmethod.name = method_fqn
          @objects_hash[method_fqn] = tmpmethod
          @objects_hash[method_fqn].parent_type = @objects_hash[class_fqn]


          
          structure_method = YARD::Registry.all(:method).find{|method| method.files.find{|file|
                                                                trace_entry[:file].end_with?(file.first)  and file.last == trace_entry[:line]
                                                              }
          }
          unless structure_method
            @objects_hash[method_fqn].signature = trace_entry[:id]
          else
            @objects_hash[method_fqn].signature = structure_method.signature
          end
        end

        @objects_hash[class_fqn].methods = []  unless @objects_hash[class_fqn].methods
        @objects_hash[class_fqn].methods << @objects_hash[method_fqn]
        @objects_hash[class_fqn].methods.uniq!
        if trace_entry[:id] == 'initialize'
          ### neues Object erzeugt
          instance = DynamixInstance.new
          
          instance.id = RubyParser::Helper.new_counter
          instance.type = @objects_hash[class_fqn]
          @objects_hash["___instance_#{instance.id}"] = instance
        end
        activation = DynamixActivation.new
        activation.id = RubyParser::Helper.new_counter
        activation_key = "___activation_#{activation.id}"
        activation.start_time = (trace_entry[:time] * 1000000).to_i.to_s
        activation.trace = dt
        if not(@activation_stack.empty?)
          activation.parent = @activation_stack.last
          @activation_stack.last.activations = [] unless @activation_stack.last.activations
          @activation_stack.last.activations << activation
        else
          @objects_hash[nil].activations = [] unless @objects_hash[nil].activations
          @objects_hash[nil].activations << activation
        end
        activation.method = @objects_hash[method_fqn]
        @objects_hash[activation_key] = activation
        @activation_stack << @objects_hash[activation_key]
      elsif trace_entry[:event] == 'return'
        @activation_stack.last.stop_time = (trace_entry[:time] * 1000000).to_i.to_s
        @activation_stack.pop
      end
    }
    
    
    
    return @objects_hash.values
    
    
  end

  
  def write_dynamix(output_file)
    string = "(Moose.Model \n"
    string << "(entity \n"
    enriched_trace(self.versions.first.trace).each{|entry|
      string << entry.to_s + "\n\n"
    }
    string << "\n)"
    string << "\n)"
#    puts string
    File.open(output_file, 'w+'){|file| file.puts string}
  end


  

end
