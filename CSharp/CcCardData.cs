//Created by Dagger -- https://github.com/gavazquez
//With the help of ArSi -- https://github.com/arsi-apli

namespace ConsoleApplication
{
    public class CcCardData
    {
        public string CaId { get; set; }
        public int ProviderCount { get; set; }
        public string[] Providers { get; set; }
        public byte[] NodeId { get; set; }
        public string RemoteId { get; set; }
        public byte[] Serial { get; set; }
        public int Uphops { get; set; }

        public override string ToString()
        {
            return $"CaId: {CaId}, Uphops: {Uphops}, Providers: {string.Join(", ", Providers)}";
        }
    }
}
