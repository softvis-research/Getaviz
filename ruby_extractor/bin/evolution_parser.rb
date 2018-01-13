load File.expand_path("#{File.dirname(__FILE__)}/../lib/parser.rb")

repo = ARGV[0]
if ARGV[1] and ARGV[1] == 'ruby18'
  parser_type = :ruby18
  output_file = ARGV[2]
else
  output_file = ARGV[1]
end

hismo_parser = RubyParser::EvolutionParser.new(repo, parser_type)
hismo_parser.parse!
hismo_parser.write_hismo(output_file)
puts hismo_parser.versions.length

