/**
 * 
 */
package com.crime.analysis;

import java.io.IOException;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;


/**
 * @author Dinesh Appavoo
 *
 */
public class CrimeDataAnalysisThreeDigit {



	public static class Map extends Mapper<LongWritable, Text, Text, IntWritable>
	{
		private static final IntWritable one=new IntWritable(1);


		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException
		{
			String line=value.toString().trim();
			String easternRegionCode ="";
			String northernRegionCode = "";
			String crimeType = "";

			Text key_word = null;
			if(!(line.contains("Crime ID") && line.contains("Reported by") && line.contains("Month") && line.contains("Easting") && line.contains("Northing")))
			{
				String[] crimeData=line.split(",");
				try{

					if(crimeData.length>7)
					{
						if((crimeType = crimeData[7].trim()) !="")
							crimeType = String.valueOf(crimeType.charAt(0));
					}
				}catch(Exception e)
				{
					crimeType = "UNKNOWNTYPE";
				}

				try{
					if((easternRegionCode = crimeData[4].trim()) !="")
						easternRegionCode = easternRegionCode.substring(0, 3);
					if((northernRegionCode = crimeData[5].trim()) !="")
						northernRegionCode = northernRegionCode.substring(0,3);

					key_word = new Text(easternRegionCode+","+northernRegionCode+","+crimeType);

				}catch(Exception e)
				{
					key_word = new Text("UNKNOWNLOC"+ crimeType);

				}
				context.write(key_word, one);

			}

		}
	}

	public static class Reduce extends Reducer<Text, IntWritable, Text, IntWritable>
	{
		public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException
		{
			int sum=0;
			for(IntWritable value : values)
			{
				sum+=value.get();
			}
			context.write(key, new IntWritable(sum));
		}
	}


	/**
	 * @param args
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException {
		// TODO Auto-generated method stub

		//Configuration conf = new Configuration(); 
		//conf.set("inParameter", toString(args));
		//Job job = new Job(conf, "wordcount");
		Job job = new Job();

		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);
		job.setJarByClass(CrimeDataAnalysisThreeDigit.class);
		job.setMapperClass(Map.class);
		job.setCombinerClass(Reduce.class);
		job.setReducerClass(Reduce.class);

		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);

		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));

		job.waitForCompletion(true);
	}
}
