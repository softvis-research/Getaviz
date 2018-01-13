### UNFERTIG
### UNFERTIG
### UNFERTIG

module HismoOutput


  class HismoNamespaceHistory
    include FamixOutput
    attr_accessor :id, :name, :containing_namespace_history, :namespace_histories, :namespace_versions, :class_histories
    def to_s
      string = "(HISMO.NamespaceHistory \n"
      string << "	(id: #{id})\n"
      string << " (containingNamespaceHistory (ref: #{self.containing_namespace_history.id})) \n" if self.containing_namespace_history
      string << " (namespaceHistories #{self.namespace_histories.uniq.collect{|ch| "(ref: " + ch.id.to_s + " )"}.join(" ")} ) \n" if self.namespace_histories and self.namespace_histories.length > 0
      string << " (namespaceVersions #{self.namespace_versions.uniq.collect{|ch| "(ref: " + ch.id.to_s + " )"}.join(" ")} ) \n" if self.namespace_versions
      string << " (classHistories #{self.class_histories.uniq.collect{|ch| "(ref: " + ch.id.to_s + " )"}.join(" ")} ) \n" if self.class_histories
      string << ")\n"
      return string
    end
  end

  class HismoNamespaceVersion
    include FamixOutput
    attr_accessor :id, :name, :parent_history, :class_versions, :commit_id, :timestamp, :yard_object
    def to_s

      famix_element = ""
      if yard_object
        famix_element = to_famix(yard_object)
        famix_id = RubyParser::Helper.get_id_for_object(yard_object)
      end
      
      string = "(HISMO.NamespaceVersion \n"
      string << "	(id: #{id})\n"
      string << "	(name '#{name.to_s.strip}')\n"
      string << "	(parentHistory (ref: #{parent_history} ))\n"
      string << " (classVersions #{self.class_versions.uniq.collect{|ch| "(ref: " + ch.id.to_s + " )"}.join(" ")} ) \n" if self.class_versions
      string << " (versionEntity (ref: #{famix_id})) \n" if yard_object
      string << " (timestamp '#{self.timestamp}') \n"
      string << " (commitId '#{self.commit_id}') \n" 
      string << ")\n\n"
      string << famix_element 
      return string
    end
  end
  
  
  class HismoClassHistory
    include FamixOutput
    attr_accessor :id, :name, :containing_namespace_history, :class_versions, :method_histories, :attribute_histories, :constant_histories
    def to_s
      string = "(HISMO.ClassHistory \n"
      string << "	(id: #{id})\n"
#      string << "	(name: #{name})\n"
      string << " (classVersions #{self.class_versions.uniq.collect{|ch| "(ref: " + ch.id.to_s + " )"}.join(" ")} ) \n" if self.class_versions
      string << " (containingNamespaceHistory (ref: #{self.containing_namespace_history.id})) \n" if self.containing_namespace_history
      string << " (methodHistories #{self.method_histories.uniq.collect{|ch| "(ref: " + ch.id.to_s + " )"}.join(" ")} ) \n" if self.method_histories
      string << " (attributeHistories #{self.attribute_histories.uniq.collect{|ch| "(ref: " + ch.id.to_s + " )"}.join(" ")} ) \n" if self.attribute_histories
      # string << " (constantHistories #{self.constant_histories.uniq.collect{|ch| "(ref: " + ch.id.to_s + " )"}.join(" ")} ) \n" if self.constant_histories
      string << ")\n"
    end
  end

  class HismoClassVersion
    include FamixOutput
    attr_accessor :id, :name, :parent_history, :commit_id, :timestamp, :yard_object
    def to_s

      famix_element = ""

      if yard_object
        famix_element = to_famix(yard_object)
        famix_id = RubyParser::Helper.get_id_for_object(yard_object)        
      end
      
      string = "(HISMO.ClassVersion \n"
      string << "	(id: #{id})\n"
      string << "	(name '#{name.to_s.strip}')\n"
      string << "	(parentHistory (ref: #{parent_history} ))\n"
      string << " (versionEntity (ref: #{famix_id} )) \n" if yard_object
      string << " (timestamp '#{self.timestamp}') \n"
      string << " (commitId '#{self.commit_id}') \n" 

      string << ")\n\n"
      string << famix_element
      return string
      
    end
  end
  

  class HismoMethodHistory
    include FamixOutput
    attr_accessor :id, :name, :min_loc, :max_loc, :containing_class_history, :method_versions
    def to_s
      string = "(HISMO.MethodHistory \n"
      string << "	(id: #{id})\n"
#      string << "	(name: #{name})\n"
      string << " (methodVersions #{self.method_versions.uniq.collect{|ch| "(ref: " + ch.id.to_s + " )"}.join(" ")} ) \n" if self.method_versions
      string << " (containingClassHistory (ref: #{self.containing_class_history.id})) \n" if self.containing_class_history
      string << " (maxNumberOfStatements #{self.max_loc}) \n" if self.max_loc
      string << " (minNumberOfStatements #{self.min_loc}) \n" if self.min_loc
      string << ")\n"
      return string
    end
  end

  class HismoMethodVersion
    include FamixOutput
    attr_accessor :id, :name, :parent_history, :loc, :commit_id, :timestamp, :yard_object
    def to_s

      famix_element = ""
      if yard_object
        famix_element = to_famix(yard_object)
        famix_id = RubyParser::Helper.get_id_for_object(yard_object)
      end
      string = "(HISMO.MethodVersion \n"
      string << "	(id: #{id})\n"
      string << "	(name '#{name.to_s.strip}')\n"
      string << "	(parentHistory (ref: #{parent_history} ))\n"
      string << " (versionEntity (ref: #{famix_id} )) \n" if yard_object
      string << " (timestamp '#{self.timestamp}') \n"
      string << " (commitId '#{self.commit_id}') \n" 
      string << " (evolutionNumberOfStatements #{self.loc}) \n"
      string << ")\n\n"
      string << famix_element
      return string

    end
  end
  

  class HismoAttributeHistory
    include FamixOutput
    attr_accessor :id, :name, :containing_class_history, :attribute_versions
    def to_s
      string = "(HISMO.AttributeHistory \n"
      string << "	(id: #{id})\n"
#      string << "	(name '#{name.to_s.strip}')\n"
      string << " (attributeVersions #{self.attribute_versions.uniq.collect{|ch| "(ref: " + ch.id.to_s + " )"}.join(" ")} ) \n" if self.attribute_versions
      string << " (containingClassHistory (ref: #{self.containing_class_history.id})) \n" if self.containing_class_history
      string << ")\n"
      return string
    end
  end

  class HismoAttributeVersion
    include FamixOutput
    attr_accessor :id, :name, :parent_history, :commit_id, :timestamp, :yard_object
    def to_s

      famix_element = ""
      if yard_object
        famix_element = to_famix(yard_object)
        famix_id = RubyParser::Helper.get_id_for_object(yard_object)
      end

      string = "(HISMO.AttributeVersion \n"
      string << "	(id: #{id})\n"
      string << "	(name '#{name.to_s.strip}')\n"
      string << "	(parentHistory (ref: #{parent_history} ))\n"
      string << " (versionEntity (ref: #{famix_id} )) \n" if yard_object
      string << " (timestamp '#{self.timestamp}') \n"
      string << " (commitId '#{self.commit_id}') \n" 

      string << ")\n\n"
      string << famix_element
      return string
    end
  end
  
  class HismoConstantHistory
    include FamixOutput
    attr_accessor :id, :name, :containing_class_history, :constant_versions
    def to_s
      return ""
      string = "(HISMO.ConstantHistory \n"
      string << "	(id: #{id})\n"
#      string << "	(name '#{name.to_s.strip}')\n"
      string << " (constantVersions #{self.constant_versions.uniq.collect{|ch| "(ref: " + ch.id.to_s + " )"}.join(" ")} ) \n" if self.constant_versions
      string << " (containingClassHistory (ref: #{self.containing_class_history.id})) \n" if self.containing_class_history
      string << ")\n"
      return string
    end
  end

  class HismoConstantVersion
    include FamixOutput
    attr_accessor :id, :name, :parent_history, :commit_id, :timestamp, :yard_object
    def to_s
      return ""
      famix_element = to_famix(yard_object)
      famix_id = RubyParser::Helper.get_id_for_object(yard_object)

      string = "(HISMO.ConstantVersion \n"
      string << "	(id: #{id})\n"
      string << "	(name '#{name.to_s.strip}')\n"
      string << "	(parentHistory (ref: #{parent_history} ))\n"
      string << " (versionEntity (ref: #{famix_id} )) \n" if famix_id
      string << " (timestamp '#{self.timestamp}') \n"
      string << " (commitId '#{self.commit_id}') \n" 

      string << ")\n\n"
      string << famix_element

      return string
    end
  end
  

  
  def write_hismo(output_file, version = 0)
    
    
    File.open(output_file, 'w+'){|file|
      file.puts "("
      @all_objects.each{|o|
        file.puts o.to_s
      }
      file.puts ")" 
    }
  end


  

end
