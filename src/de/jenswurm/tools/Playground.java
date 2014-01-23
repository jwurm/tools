package de.jenswurm.tools;

import java.util.List;

import de.jenswurm.tools.JaQL.Tuple;

public class Playground {
public static void main(String[] args) {
//	List<Tuple<String,String,String>> join = new FileJoiner().join("c:/temp/paxnamechanges.txt", ";", 1, "c:/temp/h4umodifies.txt", ";", 6);
//	for (Tuple<String, String, String> tuple : join) {
//		System.out.println(tuple.getRight());
//		
//	}
	
	
	List<Tuple<String,String,String>> join = new FileJoiner().leftJoin("c:/temp/namechanges.txt", ";", 6, "c:/temp/dates.txt", ";", 0);
	for (Tuple<String, String, String> tuple : join) {
		System.out.println((tuple.getRight()!=null?tuple.getRight().split(";")[1]:"???")+";"+tuple.getLeft());
		
	}
}
}
