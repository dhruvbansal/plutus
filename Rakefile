require 'rake'
require 'pathname'
require 'yaml'
require 'net/http'
require 'logger'
require 'erubis'

def project_root
  @project_root ||= Pathname.new(File.dirname(__FILE__))
end

def project_path *args
  project_root.join(*args)
end

def environment
  ENV["PLUTUS_ENV"] || "dev"
end

def all_config
  @all_config ||= YAML.load(open(project_path('config/plutus.yml')))
end

def config
  all_config[environment]
end

def log
  @log ||= Logger.new(STDERR)
end

def command *args
  cmd = args.join(' ')
  log.info(cmd)
  system cmd
end

def http_request verb, host_and_port, path, body=nil, headers={}
  connection = Net::HTTP.new(*host_and_port.split(":"))
  request    = Net::HTTP.const_get(verb.to_s.capitalize).new(path, headers)
  request.body = body if body
  log.info "#{verb.to_s.upcase} http://#{File.join(host_and_port, path)}"
  response = connection.request(request)
  puts response.body
end

def es verb, path, body=nil, headers={}
  host, port = config['elasticsearch']['hosts'].sample.split(':')
  http_request verb, "#{host}:#{port || 9200}", path, body, headers
end

def cql *args
  host, port = config['cassandra']['hosts'].sample.split(':')
  query = args.join(' ').gsub("'", "\\\\'")
  command "cqlsh -e '#{query}' #{host} #{port || 9042}"
end

def gremlin *args
  command "gremlin -e #{project_path('gremlin', *args)}.groovy"
end

def template source, destination
  template = File.read(project_path('templates', source))
  engine   = Erubis::Eruby.new(input)
  File.open(project_path(destination), 'w') { |f| f.puts(engine.result(config)) }
end

task :env do
end

namespace :elasticsearch do
  desc "Destroy the Elasticsearch index for plutus"
  task :destroy => [:env] do
    es :delete, "/#{config['elasticsearch']['index']}"
  end
end

namespace :cassandra do
  desc "Destroy the Cassandra keyspace for plutus"
  task :destroy => [:env] do
    cql "DROP KEYSPACE #{config['cassandra']['keyspace']}"
  end
end

namespace :config do

  desc "Generate configuration files for TitanDB"
  task :titan   => [:env] do
    template("plutus.properties.erb", "config/#{environment}/plutus.properties")
  end

  desc "Generate configuration files for Rexster"
  task :rexster => [:env] do
    template("rexster.xml.erb" "config/#{environment}/rexster.xml")
  end
  
end

namespace :titan do
  
  desc "Create the TitanDB graph"
  task :create => [:env] do
    gremlin "create"
  end

  desc "Load data naively into the graph"
  task :load => [:env] do
    gremlin "load"
  end

  desc "Delete all data!"
  task :delete => [:env] do
    gremlin "delete"
  end
  
  desc "Destroy all databases!"
  task :destroy => ["elasticsearch:destroy", "cassandra:destroy"]

  desc "Delete and reload all data"
  task :reload => [:delete, :load]

  desc "Destroy, recreate and reload all data"
  task :rebuild => [:destroy, :create, :load]
  
end
