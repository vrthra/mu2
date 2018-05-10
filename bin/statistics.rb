#!/usr/bin/env ruby

n_killed = {}
n_total = 0
Dir["build/*.lst"].each do |lst|
  lines = File.readlines(lst).map{|x|x.strip}
  next if lines.length != 1_000_000
  killing_tests = lines.select{|x| x != '1'}
  n_killed[killing_tests.length] ||= []
  n_killed[killing_tests.length] << lst
  n_total += 1
  STDERR.print '.'
  #puts "#{lst}: #{killing_tests.length}"
end

n_atleast = {}
n_killed.keys.sort.each do |k|
  (1..k).each do |i|
    n_atleast[i] ||= 0
    n_atleast[i] += n_killed[k].length
  end
end
n_atleast.keys.sort.reverse.each do |k|
  puts "#{k},#{n_atleast[k]}"
end
